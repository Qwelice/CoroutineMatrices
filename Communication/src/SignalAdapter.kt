import signals.Signal

interface SignalAdapter {
    suspend fun inputSignal(signal: Signal, sio: SocketIO)
}