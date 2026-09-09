package livefootball.footballstreamning.fifaworldcup.utilities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;

import livefootball.footballstreamning.fifaworldcup.R;

public class Utils {
    private static boolean isDialogShowing = false;

    public static void showCustomDialog(Context context, String title, String message,
                                        String positiveText, String negativeText,
                                        boolean setNegative, boolean setCancelable,
                                        String imageUrl,
                                        View.OnClickListener positiveListener,
                                        View.OnClickListener negativeListener) {
        if (isDialogShowing) return;
        try {
            if (!(context instanceof Activity)) return;
            Activity activity = (Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) {
                return; // Activity is no longer valid, don't show dialog
            }

            // Inflate the custom layout
            View dialogView = LayoutInflater.from(context).inflate(R.layout.custom_dialog_style, null);

            // Create the dialog
            Dialog dialog = new Dialog(context);
            dialog.setContentView(dialogView);

            if (dialog.getWindow() != null) {
                Window window = dialog.getWindow();
                window.setDimAmount(0.7f);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;

                int marginInPixels = (int) (32 * context.getResources().getDisplayMetrics().density);
                dialog.getWindow().setAttributes(params);
                dialog.getWindow().getDecorView().setPadding(marginInPixels, 0, marginInPixels, 0);
            }

            // Set icon if provided
            ImageView dialogIcon = dialogView.findViewById(R.id.dialogIcon);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                dialogIcon.setVisibility(View.VISIBLE);
                Glide.with(context).load(imageUrl).into(dialogIcon);
            } else {
                dialogIcon.setVisibility(View.GONE);
            }

            // Set title and message
            TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
            TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
            dialogTitle.setText(title);
            dialogMessage.setText(message);

            // Set buttons
            AppCompatButton positiveButton = dialogView.findViewById(R.id.positiveButton);
            AppCompatButton negativeButton = dialogView.findViewById(R.id.negativeButton);

            positiveButton.setText(positiveText);
            positiveButton.setOnClickListener(v -> {
                if (positiveListener != null) positiveListener.onClick(v);
                dialog.dismiss();
            });

            if (setNegative) {
                negativeButton.setVisibility(View.VISIBLE);
                negativeButton.setText(negativeText);
                negativeButton.setOnClickListener(v -> {
                    if (negativeListener != null) negativeListener.onClick(v);
                    dialog.dismiss();
                });
            } else {
                negativeButton.setVisibility(View.GONE);
            }

            // Dialog cancel options
            dialog.setCancelable(setCancelable);
            dialog.setCanceledOnTouchOutside(setCancelable);

            dialog.setOnDismissListener(d -> isDialogShowing = false);

            // Safely show the dialog
            if (!activity.isFinishing() &&
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !activity.isDestroyed())) {
                dialog.show();
                isDialogShowing = true;
            }
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace(); // log and ignore instead of crashing
        }
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            // For Android 10 and above (API level 29+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        return true;
                    }
                }
            } else {
                // For Android versions below 10 (API level 29)
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Animates a view (like a live dot) with a continuous pulse effect.
     */
    public static void animateLiveDot(View view) {
        if (view == null) return;
        
        AlphaAnimation pulse = new AlphaAnimation(1.0f, 0.2f);
        pulse.setDuration(800);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(Animation.INFINITE);
        view.startAnimation(pulse);
    }


    /**
     * Continuous pulse and scale animation for social icons
     */
    public static void animateSocialIcon(View view) {
        if (view == null) return;

        android.view.animation.AnimationSet animationSet = new android.view.animation.AnimationSet(true);

        android.view.animation.ScaleAnimation scale = new android.view.animation.ScaleAnimation(
                1.0f, 1.15f, 1.0f, 1.15f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scale.setDuration(1000);
        scale.setRepeatMode(Animation.REVERSE);
        scale.setRepeatCount(Animation.INFINITE);

        AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.7f);
        alpha.setDuration(1000);
        alpha.setRepeatMode(Animation.REVERSE);
        alpha.setRepeatCount(Animation.INFINITE);

        animationSet.addAnimation(scale);
        animationSet.addAnimation(alpha);
        view.startAnimation(animationSet);
    }


}
