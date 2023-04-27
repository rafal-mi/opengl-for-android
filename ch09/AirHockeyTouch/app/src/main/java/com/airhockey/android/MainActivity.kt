package com.airhockey.android

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
import com.airhockey.android.ui.theme.AirHockeyTouchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AirHockeyTouchTheme {
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
    val airHockeyRenderer = AirHockeyRenderer(LocalContext.current)

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
                    setRenderer(airHockeyRenderer)
                    setOnTouchListener { v, event ->
//                        if(event.action == MotionEvent.ACTION_DOWN)
//                            v.performClick()
                        val normalizedX = (event.x / v.width.toFloat()) * 2 - 1
                        val normalizedY = -(event.y / v.height.toFloat() * 2 - 1)
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            v.performClick()
                            this.queueEvent {
                                airHockeyRenderer.handleTouchPress(
                                    normalizedX, normalizedY
                                )
                            }
                        } else if (event.action == MotionEvent.ACTION_MOVE) {
                            this.queueEvent {
                                airHockeyRenderer.handleTouchDrag(
                                    normalizedX, normalizedY
                                )
                            }
                        }
                        true
                    }

                }
            }
        )
    }
}

