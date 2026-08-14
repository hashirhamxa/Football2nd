---
name: update-app-logo
description: >-
  Automates and guides the process of replacing an Android application's launcher icons, 
  including adaptive icons, legacy mipmaps, and Google Play Store assets.
---

# Updating Android App Launcher Icons

This skill guides the process of updating Android application launcher icons. It explains how to structure assets for Adaptive Icons (introduced in Android 8.0 / API 26) and legacy mipmaps, and provides an automated Python script to generate all required densities.

---

## 1. Icon Concepts & Guidelines

### Adaptive Icons (API 26+)
Adaptive icons are split into two layers:
1. **Background Layer**: Can be a solid color or a background image/pattern. Standard size is `512x512` px (or `108x108` dp).
2. **Foreground Layer**: Must have a transparent background. Standard size is `512x512` px.
   - **Safe Zone**: Devices apply various masks (circle, squircle, teardrop) over adaptive icons. To prevent key elements (like text or logos) from being cropped, all foreground content must fit within a centered circular **Safe Zone** of diameter **66dp** (approx. **61%** of the canvas, or **312x312 px** on a `512x512 px` canvas).

### Legacy Icons (Pre-API 26)
Older Android versions use static mipmap images:
- **`ic_launcher.webp`**: Standard square launcher icon.
- **`ic_launcher_round.webp`**: Circular launcher icon.

---

## 2. Resource Directory Structure

Ensure the following files are placed correctly in your Android project resources (`app/src/main/res/`):

- **Adaptive XML Configs** (`mipmap-anydpi-v26/`):
  - `ic_launcher.xml` and `ic_launcher_round.xml` referencing drawable layers:
    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
        <background android:drawable="@drawable/ic_launcher_background" />
        <foreground android:drawable="@drawable/ic_launcher_foreground" />
    </adaptive-icon>
    ```
- **Adaptive Drawables** (`drawable/`):
  - `ic_launcher_background.png`
  - `ic_launcher_foreground.png`
- **Legacy Mipmaps** (across densities):
  - `mipmap-mdpi/` (48x48 px)
  - `mipmap-hdpi/` (72x72 px)
  - `mipmap-xhdpi/` (96x96 px)
  - `mipmap-xxhdpi/` (144x144 px)
  - `mipmap-xxxhdpi/` (192x192 px)

---

## 3. Automated Icon Generator Script

You can use the following Python script to automatically extract the foreground, center it in the safe zone, and generate all legacy densities and store assets.

### Requirements
- **Python 3.x**
- **Pillow**: `pip install Pillow`

### Usage
Save this script as `generate_icons.py` and run it:
```bash
python generate_icons.py --flat /path/to/flat_logo.png --rounded /path/to/rounded_logo.png --out /path/to/project_dir
```

### Python Script Code (`generate_icons.py`)

```python
import os
import argparse
from PIL import Image

def extract_foreground(flat_img_path, threshold_low=90.0, threshold_high=130.0):
    """
    Extracts the foreground (TV and text) from a flat icon by masking out the background color.
    Uses Euclidean distance thresholding in RGB space for antialiasing.
    """
    img = Image.open(flat_img_path).convert("RGBA")
    bg_r, bg_g, bg_b, _ = img.getpixel((0, 0))
    data = img.getdata()
    
    new_data = []
    for item in data:
        r, g, b, a = item
        dist = ((r - bg_r) ** 2 + (g - bg_g) ** 2 + (b - bg_b) ** 2) ** 0.5
        
        if dist <= threshold_low:
            new_data.append((0, 0, 0, 0))
        elif dist >= threshold_high:
            new_data.append((r, g, b, a))
        else:
            ratio = (dist - threshold_low) / (threshold_high - threshold_low)
            new_alpha = int(a * ratio)
            new_data.append((r, g, b, new_alpha))
            
    img.putdata(new_data)
    return img, (bg_r, bg_g, bg_b, 255)

def generate_assets(flat_path, rounded_path, project_root):
    # Ensure resource dirs exist
    res_dir = os.path.join(project_root, "app", "src", "main", "res")
    drawable_dir = os.path.join(res_dir, "drawable")
    store_dir = os.path.join(project_root, "store_assets")
    
    os.makedirs(drawable_dir, exist_ok=True)
    os.makedirs(store_dir, exist_ok=True)
    
    # 1. Extract foreground and detect background color
    print("Extracting foreground...")
    fg_extracted, bg_color = extract_foreground(flat_path)
    
    # 2. Save play store flat & rounded assets
    print("Saving store assets...")
    img_flat = Image.open(flat_path).convert("RGBA").resize((512, 512), Image.Resampling.LANCZOS)
    img_flat.save(os.path.join(store_dir, "playstore_icon_flat.png"), "PNG")
    
    img_rounded = Image.open(rounded_path).convert("RGBA").resize((512, 512), Image.Resampling.LANCZOS)
    img_rounded.save(os.path.join(store_dir, "playstore_icon_rounded.png"), "PNG")
    
    # 3. Generate adaptive background and foreground
    print("Generating adaptive drawables...")
    bg_adaptive = Image.new("RGBA", (512, 512), bg_color)
    bg_adaptive.save(os.path.join(drawable_dir, "ic_launcher_background.png"), "PNG")
    
    # Scale foreground to fit Android's 62.5% safe zone (320px inside 512px)
    fg_scaled = fg_extracted.resize((320, 320), Image.Resampling.LANCZOS)
    fg_canvas = Image.new("RGBA", (512, 512), (0, 0, 0, 0))
    fg_canvas.paste(fg_scaled, ((512 - 320) // 2, (512 - 320) // 2), fg_scaled)
    fg_canvas.save(os.path.join(drawable_dir, "ic_launcher_foreground.png"), "PNG")
    
    # 4. Generate legacy mipmaps
    mipmap_densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192
    }
    
    print("Generating legacy mipmaps...")
    for folder, size in mipmap_densities.items():
        folder_path = os.path.join(res_dir, folder)
        os.makedirs(folder_path, exist_ok=True)
        
        # Square legacy
        flat_resized = img_flat.resize((size, size), Image.Resampling.LANCZOS)
        flat_resized.save(os.path.join(folder_path, "ic_launcher.webp"), "WEBP")
        
        # Round legacy
        round_resized = img_rounded.resize((size, size), Image.Resampling.LANCZOS)
        round_resized.save(os.path.join(folder_path, "ic_launcher_round.webp"), "WEBP")
        
    print("All assets generated successfully!")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Android App Launcher Icon Generator")
    parser.add_argument("--flat", required=True, help="Path to 512x512 flat square icon source")
    parser.add_argument("--rounded", required=True, help="Path to 512x512 rounded icon source")
    parser.add_argument("--out", required=True, help="Android project root directory")
    args = parser.parse_args()
    
    generate_assets(args.flat, args.rounded, args.out)
```
