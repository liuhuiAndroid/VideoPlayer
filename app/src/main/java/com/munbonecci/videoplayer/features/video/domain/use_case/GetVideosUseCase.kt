package com.munbonecci.videoplayer.features.video.domain.use_case

import android.content.Context
import com.munbonecci.videoplayer.commons.Resource
import com.munbonecci.videoplayer.domain.VideoEntity
import com.munbonecci.videoplayer.domain.VideoResultEntity
import com.munbonecci.videoplayer.features.video.domain.use_case.VideoConstants.ERROR_CODE_SERVICE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetVideosUseCase @Inject constructor(private val context: Context) {

    // 在 Kotlin 中，invoke 是一个特殊的运算符方法，允许你像调用函数一样调用对象。
    operator fun invoke(): Flow<Resource<List<VideoEntity>>> = flow {
        emit(Resource.Loading())
        emit(Resource.Success(getVideosFakeData))
    }.catch {
        emit(Resource.Error(ERROR_CODE_SERVICE))
    }
}

object VideoConstants {
    const val ERROR_CODE_SERVICE = "-9"
}

val getVideosFakeData: List<VideoEntity>
    get() = listOf(
        VideoEntity(
            name = "1- Bunny Video",
            description = "This Video is used for Testing, the content is not mine and is free to use",
            extraInfo = "#extra @test, #extra @test, #extra @test, #extra @test, #extra @test, #extra @test",
            videoResultEntity = VideoResultEntity(
                1,
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                "Bunny",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            )
        )
    )