package app.web.drjacky.binderexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import app.web.drjacky.binderexample.ui.theme.BinderExampleTheme
import app.web.drjacky.binderexample.utils.collectIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.newSingleThreadContext

class MainActivity : ComponentActivity() {
    private val thread = newSingleThreadContext("MyThread")
    private val communicator = Communicator(thread)
    private val totalResponses = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BinderExampleTheme {
                val modifier: Modifier = Modifier
                Surface(
                    modifier = modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val responsesCount = remember { mutableIntStateOf(0) }
                    val responseList = remember { mutableStateListOf("") }

                    // Dynamic waiting message
                    LaunchedEffect(Unit) {
                        var dots = 1
                        while (responsesCount.intValue < 9) {
                            responseList[0] = "Waiting for response" + ".".repeat(dots)
                            dots = (dots % 3) + 1
                            delay(500)
                        }

                        responseList.removeAt(0)
                    }

                    // Collect responses from communicator
                    LaunchedEffect(Unit) {
                        communicator.processResponsesChannel().collectIn(this@MainActivity) {
                            delay(3000)
                            responseList.add(it)
                            println("ResponseChannel $it received")
                            responsesCount.intValue++
                            println("responsesCount: ${responsesCount.intValue}")
                        }
                        communicator.processResponsesFlow().collectIn(this@MainActivity) {
                            delay(3000)
                            responseList.add(it)
                            println("ResponseFlow $it received")
                        }
                    }
                    App(modifier, responseList)
                }
            }
        }
        communicator.start("First call", totalResponses)
        communicator.start("Second call", totalResponses)
        communicator.start("Third call", totalResponses)
        communicator.shutdown()
    }
}

@Composable
fun App(
    modifier: Modifier = Modifier,
    responseList: SnapshotStateList<String>,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(responseList) {
            Text(
                text = it,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    val responseResult by remember { mutableStateOf("Waiting for response...") }

    BinderExampleTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hello ${responseResult}!",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }
    }
}