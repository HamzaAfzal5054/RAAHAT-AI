package pk.raahat.ai

enum class Severity { LOW, MODERATE, HIGH, SEVERE, CRITICAL }
data class CitizenReport(val location:String, val waterLevel:String, val situations:Set<String>, val description:String, val photo:Boolean)
data class WeatherSignal(val rainfallMmHr:Int)
data class TrafficSignal(val congestion:Int, val speed:Int, val normalSpeed:Int)
data class DrainageSignal(val capacity:Int)
data class RoadRisk(val name:String, val vulnerability:Int)
data class SeverityAssessment(val score:Int, val severity:Severity, val confidence:Int, val reasoning:List<String>)
data class PredictedZone(val name:String, val probability:Int, val eta:String)
data class ResponseAction(val title:String, val detail:String)
data class ResponsePlan(val name:String, val congestionReduction:Int, val protected:Int, val cost:String, val actions:List<ResponseAction>, val recommended:Boolean=false)
data class DispatchTicket(val id:String, val team:String, val priority:Severity, val eta:Int, val status:String)
data class ResidentAlert(val message:String, val recipients:Int)
data class ResourceUnit(val name:String, val type:String, val distance:Double, val available:Boolean, val eta:Int)
data class IncidentTimelineEvent(val time:String, val title:String)
data class FloodIncident(val name:String, val assessment:SeverityAssessment, val reports:Int, val weather:WeatherSignal, val traffic:TrafficSignal, val drainage:DrainageSignal, val exposure:Int, val predictions:List<PredictedZone>)
data class ResponseSimulation(val beforeRisk:Int, val afterRisk:Int, val beforeTraffic:Int, val afterTraffic:Int, val beforeExposure:Int, val afterExposure:Int)

class SignalFusionEngine {
    fun assess(reportCount:Int, photo:Boolean, situations:Set<String>, rain:Int, congestion:Int, drainageCapacity:Int, road:Int): SeverityAssessment {
        var citizen = when(reportCount){ 0->0; 1->20; in 2..3->50; in 4..6->75; else->100 }
        if(photo) citizen += 10
        if("Vehicle stuck" in situations) citizen += 15
        if("Person at risk" in situations) citizen += 25
        if("Underpass flooding" in situations) citizen += 15
        if("Water rising quickly" in situations) citizen += 15
        citizen = citizen.coerceAtMost(100)
        val weather = when { rain <= 5 -> 10; rain <= 15 -> 35; rain <= 30 -> 70; else -> 100 }
        val traffic = when { congestion < 20 -> 10; congestion < 40 -> 40; congestion < 70 -> 70; else -> 100 }
        val drainage = when { drainageCapacity > 70 -> 10; drainageCapacity > 35 -> 40; drainageCapacity > 10 -> 80; else -> 100 }
        val score = (citizen*.30 + weather*.25 + traffic*.20 + drainage*.15 + road*.10).toInt()
        val severity = when(score){ in 0..29->Severity.LOW; in 30..49->Severity.MODERATE; in 50..69->Severity.HIGH; in 70..84->Severity.SEVERE; else->Severity.CRITICAL }
        val spread = listOf(citizen,weather,traffic,drainage,road).max()-listOf(citizen,weather,traffic,drainage,road).min()
        val confidence = (96-spread/5 + if(reportCount>=4) 4 else 0).coerceIn(42,96)
        return SeverityAssessment(score,severity,confidence,listOf("$reportCount citizen reports cluster within 420 metres","Rainfall intensity reached $rain mm/hr","Traffic speed dropped by $congestion%","Drainage capacity is critically reduced to $drainageCapacity%"))
    }
}
