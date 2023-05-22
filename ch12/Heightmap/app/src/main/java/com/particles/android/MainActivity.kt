package com.particles.android

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.particles.android.ui.theme.ParticlesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ParticlesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainContent()
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    val renderer = ParticlesRenderer(LocalContext.current)

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.app_name))
            }
        )
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
            //.weight(8f)
            ,
            factory = {
                GLSurfaceView(it).apply {
                    setEGLContextClientVersion(2)
                    setRenderer(renderer)
                    var previousX = 0f
                    var previousY = 0f
                    setOnTouchListener { v, event ->

                        if (event.action == MotionEvent.ACTION_DOWN) {
                            v.performClick()
                            previousX = event.x
                            previousY = event.y
                        } else if (event.action == MotionEvent.ACTION_MOVE) {
                            val deltaX = event.x - previousX
                            val deltaY = event.y - previousY
                            this.queueEvent {
                                renderer.handleTouchDrag(deltaX, deltaY)
                            }
                        }
                        true
                    }
                }
            }
        )
    }
}
