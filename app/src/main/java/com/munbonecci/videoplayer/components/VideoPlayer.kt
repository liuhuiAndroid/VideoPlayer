package com.munbonecci.videoplayer.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.munbonecci.videoplayer.commons.Constants.VIDEO_PLAYER_TAG
import com.munbonecci.videoplayer.domain.VideoResultEntity

/**
 * Composable function that displays a video player using ExoPlayer with Jetpack Compose.
 *
 * @param video The [VideoResultEntity] representing the video to be played.
 * @param playingIndex State that represents the current playing index.
 * @param onVideoChange Callback function invoked when the video changes.
 * @param isVideoEnded Callback function to determine whether the video has ended.
 * @param modifier Modifier for styling and positioning.
 *
 * @OptIn annotation to UnstableApi is used to indicate that the API is still experimental and may
 * undergo changes in the future.
 *
 * @SuppressLint annotation is used to suppress lint warning for the usage of OpaqueUnitKey.
 *
 * @ExperimentalAnimationApi annotation is used for the experimental Animation API usage.
 */
@OptIn(UnstableApi::class)
@SuppressLint("OpaqueUnitKey")
@ExperimentalAnimationApi
@Composable
fun VideoPlayer(
    video: VideoResultEntity,
    playingIndex: State<Int>,
    onVideoChange: (Int) -> Unit,
    isVideoEnded: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Get the current context
    val context = LocalContext.current

    // Mutable state to control the visibility of the video title
    val visible = remember { mutableStateOf(true) }

    // Mutable state to hold the video title
    val videoTitle = remember { mutableStateOf(video.name) }

    // Create a list of MediaItems for the ExoPlayer
    // 创建一个 MediaItems 列表来保存视频信息
    val mediaItems = arrayListOf<MediaItem>()
    mediaItems.add(
        MediaItem.Builder()
            .setUri(video.video)
            .setMediaId(video.id.toString())
            .setTag(video)
            .setMediaMetadata(MediaMetadata.Builder().setDisplayTitle(video.name).build())
            .build()
    )

    // Initialize ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // 为播放器设置一组要播放的媒体项（mediaItems）
            this.setMediaItems(mediaItems)
            // 准备播放器以便开始播放。此方法会加载媒体数据，使其可用于播放。
            this.prepare()
            this.addListener(object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    super.onEvents(player, events)
                    // Hide video title after playing for 200 milliseconds
                    // 当视频播放到 200 毫秒后，隐藏视频标题
                    if (player.contentPosition >= 200) visible.value = false
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    // Callback when the video changes
                    // 当视频切换时触发以下逻辑
                    onVideoChange(this@apply.currentPeriodIndex)
                    visible.value = true
                    videoTitle.value = mediaItem?.mediaMetadata?.displayTitle.toString()
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    // Callback when the video playback state changes to STATE_ENDED
                    // 当播放状态变为 STATE_ENDED（播放结束）时，调用 isVideoEnded 回调，通知视频播放已结束
                    if (playbackState == ExoPlayer.STATE_ENDED) {
                        isVideoEnded.invoke(true)
                    }
                }
            })
        }
    }

    // Seek to the specified index and start playing
    // 将播放位置设置到指定的媒体项和时间点，C.TIME_UNSET：表示播放从该媒体项的默认起始位置开始
    exoPlayer.seekTo(playingIndex.value, C.TIME_UNSET)
    exoPlayer.playWhenReady = true

    // Add a lifecycle observer to manage player state based on lifecycle events
    // 添加生命周期观察器
    LocalLifecycleOwner.current.lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_START -> {
                    // Start playing when the Composable is in the foreground
                    // 当 Composable 进入前台时，检查播放器是否正在播放
                    if (exoPlayer.isPlaying.not()) {
                        exoPlayer.play()
                    }
                }

                Lifecycle.Event.ON_STOP -> {
                    // Pause the player when the Composable is in the background
                    // 当 Composable 进入后台时，调用 exoPlayer.pause() 暂停播放
                    exoPlayer.pause()
                }

                else -> {
                    // Nothing
                }
            }
        }
    })

    // Column Composable to contain the video player
    Column(modifier = modifier.background(Color.Black)) {
        // DisposableEffect to release the ExoPlayer when the Composable is disposed
        // DisposableEffect 的主要作用是确保在 Composable 生命周期结束时（即不再需要时）执行清理逻辑，比如释放资源或取消订阅
        DisposableEffect(
            // 在 View 中使用 Compose：https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/compose-in-views?hl=zh-cn
            // 传统 View 可以用 androidx.compose.ui.platform.ComposeView
            // val composeView = findViewById<ComposeView>(R.id.composeView)
            // composeView.setContent { Text(text = "Hello Compose!") }

            // 在 Compose 中使用 View：https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/views-in-compose?hl=zh-cn
            // AndroidView 用于将传统的 Android View 嵌入到 Compose UI 中
            AndroidView(
                modifier = modifier
                    .testTag(VIDEO_PLAYER_TAG),
                factory = {
                    // AndroidView to embed a PlayerView into Compose
                    PlayerView(context).apply {
                        player = exoPlayer
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        // Set resize mode to fill the available space
                        // 设置缩放模式，填充可用空间
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                        // Hide unnecessary player controls
                        // 隐藏播放控制按钮
                        setShowNextButton(false)
                        setShowPreviousButton(false)
                        setShowFastForwardButton(false)
                        setShowRewindButton(false)
                    }
                },
                update = {
                    // 在 Recompose 过程中被调用，用于更新 AndroidView 的属性
                })
        ) {
            // Dispose the ExoPlayer when the Composable is disposed
            onDispose {
                exoPlayer.release()
            }
        }
    }
}

@ExperimentalAnimationApi
@Preview(showBackground = true)
@Composable
fun ShowVideoPreview() {
    val video = VideoResultEntity(
        1,
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        "Bunny",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    )
    val playingIndex = remember {
        mutableIntStateOf(0)
    }

    fun onTrailerChange(index: Int) {
        playingIndex.intValue = index
    }
    VideoPlayer(
        video = video,
        playingIndex = playingIndex,
        onVideoChange = { newIndex -> onTrailerChange(newIndex) },
        isVideoEnded = {}
    )
}