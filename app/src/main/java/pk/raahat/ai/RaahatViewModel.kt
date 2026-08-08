package pk.raahat.ai

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Screen { SPLASH, ROLE, CITIZEN, REPORT, VERIFY, COMMAND, INCIDENT, STRATEGY, EXECUTE, IMPACT, CITY, COPILOT }

class RaahatViewModel: ViewModel() {
    var screen by mutableStateOf(Screen.SPLASH); private set
    var demo by mutableStateOf(false); private set
    var selectedLevel by mutableStateOf("Knee deep")
    var selectedSituation by mutableStateOf("Underpass flooding")
    var executionStep by mutableIntStateOf(0); private set
    var verifiedSignals by mutableIntStateOf(0); private set
    var copilotText by mutableStateOf("")
    val assessment = SignalFusionEngine().assess(5,true,setOf("Vehicle stuck","Underpass flooding","Water rising quickly"),36,89,22,90)
    val incident = FloodIncident("G-10 Underpass", assessment,5,WeatherSignal(36),TrafficSignal(89,7,43),DrainageSignal(22),1240,listOf(PredictedZone("G-10/2 Street 14",72,"10–16 min"),PredictedZone("G-9 Service Road",48,"22–30 min")))
    val strategies = listOf(
        ResponsePlan("A · Close underpass",19,460,"LOW",listOf(ResponseAction("Close road","Underpass only"))),
        ResponsePlan("B · Coordinated response",53,1040,"MEDIUM",listOf(ResponseAction("Close underpass","Prevent entry"),ResponseAction("Reroute traffic","Service Road West"),ResponseAction("Dispatch D-07","Drainage team")),true),
        ResponsePlan("C · Full emergency",68,1190,"HIGH",listOf(ResponseAction("Full closure","All approaches"),ResponseAction("Multi-team deployment","Police + drainage + alerts")))
    )
    val timeline = listOf("18:41" to "Heavy rainfall alert detected","18:45" to "Traffic slowdown begins","18:46" to "First citizen report received","18:49" to "Two additional reports received","18:51" to "Drainage risk increased","18:52" to "Confidence reaches 91%","18:53" to "Escalated to CRITICAL","18:54" to "Response plan generated")
    init { viewModelScope.launch { delay(2400); screen=Screen.ROLE } }
    fun go(s:Screen){ screen=s }
    fun startDemo(){ demo=true; screen=Screen.COMMAND }
    fun verify(){ screen=Screen.VERIFY; verifiedSignals=0; viewModelScope.launch { repeat(4){ delay(650); verifiedSignals++ } } }
    fun execute(){ screen=Screen.EXECUTE; executionStep=0; viewModelScope.launch { repeat(5){ delay(750); executionStep++ }; delay(500); screen=Screen.IMPACT } }
    fun answer(q:String){ copilotText = when { "team" in q.lowercase() -> "Dispatch D-07. It is available 1.1 km away and arrives 6 minutes faster than the next drainage unit."; "don't" in q.lowercase() || "close" in q.lowercase() -> "Without closure, G-10/2 has a 72% simulated risk of secondary flooding within 16 minutes, exposing 930 more residents."; else -> "G-10 is the highest-risk area: 91/100 with 94% confidence. Four independent signals agree and drainage capacity is only 22%." } }
}
