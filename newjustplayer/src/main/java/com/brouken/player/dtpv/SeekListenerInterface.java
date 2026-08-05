package com.brouken.player.dtpv;

public interface SeekListenerInterface {
    /**
     * Called when video start reached during rewinding
     */
    void onVideoStartReached();

    /**
     * Called when video end reached during forwarding
     */
    void onVideoEndReached();
}
