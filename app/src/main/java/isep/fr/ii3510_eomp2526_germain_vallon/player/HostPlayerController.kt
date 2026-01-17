package isep.fr.ii3510_eomp2526_germain_vallon.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class HostPlayerController(context: Context) {

    private val player: ExoPlayer = ExoPlayer.Builder(context).build()

    private var onEnded: (() -> Unit)? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    onEnded?.invoke()
                }
            }
        })
    }

    fun playUrl(url: String) {
        val item = MediaItem.fromUri(url)
        player.setMediaItem(item)
        player.prepare()
        player.playWhenReady = true
    }

    fun pause() {
        player.playWhenReady = false
    }

    fun resume() {
        player.playWhenReady = true
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
    }

    fun setVolume(volume: Float) {
        player.volume = volume.coerceIn(0f, 1f)
    }

    fun isPlaying(): Boolean = player.isPlaying

    fun setOnEnded(callback: () -> Unit) {
        onEnded = callback
    }

    fun release() {
        player.release()
    }
}
