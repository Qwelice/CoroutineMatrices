import signals.Signal

interface ClientAdapter {
    suspend fun inputClientData(data: Signal, client: Client)
}