package com.star.play.media3;

import android.content.Context;

import xyz.doikki.videoplayer.player.AbstractPlayer;
import xyz.doikki.videoplayer.player.PlayerFactory;

/**
 * Creates {@link Media3Player} instances for use with DKPlayer.
 *
 * <pre>{@code
 * videoView.setPlayerFactory(new Media3PlayerFactory());
 * }</pre>
 */
public class Media3PlayerFactory extends PlayerFactory<AbstractPlayer> {

    @Override
    public AbstractPlayer createPlayer(Context context) {
        Media3Player player = new Media3Player();
        player.setAppContext(context);
        return player;
    }
}
