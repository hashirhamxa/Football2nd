package com.brouken.player;

import android.net.Uri;
import android.util.Log;

import androidx.media3.datasource.DataSpec;
import androidx.media3.exoplayer.drm.ExoMediaDrm;
import androidx.media3.exoplayer.drm.MediaDrmCallback;
import androidx.media3.exoplayer.drm.MediaDrmCallbackException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;

public class ClearKeyMediaDrmCallback implements MediaDrmCallback {
    private final String keyId;
    private final String key;

    public ClearKeyMediaDrmCallback(String keyId, String key) {
        this.keyId = keyId;
        this.key = key;
    }

    @Override
    public byte[] executeProvisionRequest(UUID uuid, ExoMediaDrm.ProvisionRequest request) {
        throw new UnsupportedOperationException("Provisioning not supported for ClearKey");
    }

    @Override
    public byte[] executeKeyRequest(UUID uuid, ExoMediaDrm.KeyRequest request) throws MediaDrmCallbackException {
        try {
            // Format the response according to ClearKey specification
            String response = String.format(
                    "{\"keys\":[{\"kty\":\"oct\",\"k\":\"%s\",\"kid\":\"%s\"}],\"type\":\"temporary\"}",
                    key, keyId
            );
            Log.e("leolog","executeKeyRequest response "+response);
            return response.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Create proper exception with required parameters
            DataSpec dataSpec = new DataSpec.Builder().setUri("").build();
            throw new MediaDrmCallbackException(
                    dataSpec,
                    Uri.EMPTY,
                    Collections.emptyMap(),
                    0,
                    new IllegalStateException("Failed to generate ClearKey response", e)
            );
        }
    }
}

