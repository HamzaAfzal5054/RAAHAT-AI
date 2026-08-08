package pk.raahat.ai

/** Provider seams keep the deterministic demo usable while live integrations are added. */
interface CitizenReportService { suspend fun submit(report: CitizenReport): String }
interface WeatherService { suspend fun current(zone: String): WeatherSignal }
interface TrafficService { suspend fun current(zone: String): TrafficSignal }
interface DrainageService { suspend fun current(zone: String): DrainageSignal }
interface AlertService { suspend fun send(alert: ResidentAlert): Boolean }
interface DispatchService { suspend fun dispatch(ticket: DispatchTicket): DispatchTicket }

class FloodPredictionEngine {
    fun predict(incident: FloodIncident) = incident.predictions
}

class ResponsePlanner {
    fun recommended(plans: List<ResponsePlan>) = plans.firstOrNull { it.recommended }
}

class SimulationEngine {
    fun run() = ResponseSimulation(91, 59, 89, 37, 1240, 310)
}

data class AIReasoning(
    val severity: Severity,
    val confidence: Int,
    val summary: String,
    val reasoning: List<String>,
    val recommendedActions: List<String>,
    val predictedNextZone: PredictedZone
)

interface AIReasoningService { suspend fun explain(incident: FloodIncident): AIReasoning }

/** Offline safety layer. A Gemini implementation can replace this without changing UI state. */
class DeterministicAIReasoningService : AIReasoningService {
    override suspend fun explain(incident: FloodIncident) = AIReasoning(
        severity = incident.assessment.severity,
        confidence = incident.assessment.confidence.coerceIn(0, 100),
        summary = "Rapidly escalating flood incident",
        reasoning = incident.assessment.reasoning,
        recommendedActions = listOf(
            "Close G-10 underpass", "Reroute through Service Road West",
            "Dispatch Drainage Unit D-07", "Alert residents within 1.5 km"
        ),
        predictedNextZone = incident.predictions.first()
    )
}

object AIOutputValidator {
    fun isValid(value: AIReasoning) = value.confidence in 0..100 &&
        value.reasoning.isNotEmpty() && value.recommendedActions.isNotEmpty() &&
        value.predictedNextZone.probability in 0..100
}
