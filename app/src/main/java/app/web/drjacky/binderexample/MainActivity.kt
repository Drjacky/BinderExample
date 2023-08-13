package app.web.drjacky.binderexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.web.drjacky.binderexample.ui.theme.BinderExampleTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import app.web.drjacky.binderexample.utils.collectIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext

@OptIn(DelicateCoroutinesApi::class)
class MainActivity : ComponentActivity() {
    private val thread = newSingleThreadContext("MyThread")
    private val communicator = Communicator(thread)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BinderExampleTheme {
                val modifier: Modifier = Modifier
                Surface(
                    modifier = modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val responseList =
                        remember { mutableStateListOf<String>("Waiting for response...") }

                    LaunchedEffect(Unit) { //Needed for the launch
                        CoroutineScope(Dispatchers.Main).launch {
                            communicator.processResponses().collectIn(this@MainActivity) {
                                responseList.add(it)
                                println("Response $it received")
                            }
                        }
                    }
                    App(modifier, responseList)
                }
            }
        }
        communicator.start("First call")
        communicator.start("Second call")
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
        itemsIndexed(responseList) { i, item ->
            println("$i - Response $item displayed")
            Text(
                text = item,
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