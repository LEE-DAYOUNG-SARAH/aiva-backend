package com.aiva.chat.dto.ai

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.util.*

sealed class AiResponse {
    
    data class Start(
        @JsonProperty("log_id")
        val logId: String,
        
        @JsonProperty("request_id") 
        val requestId: String,
        
        @JsonProperty("timestamp")
        val timestamp: LocalDateTime
    ) : AiResponse()
    
    data class Chunk(
        @JsonProperty("delta")
        val delta: String
    ) : AiResponse()
    
    data class Metadata(
        @JsonProperty("source_docs")
        val sourceDocs: String? = null,
        
        @JsonProperty("summary")
        val summary: String? = null
    ) : AiResponse()
    
    data class Done(
        @JsonProperty("finish_reason")
        val finishReason: String,
        
        @JsonProperty("duration_ms")
        val durationMs: Long
    ) : AiResponse()
    
    data class Error(
        @JsonProperty("code")
        val code: Int,
        
        @JsonProperty("message")
        val message: String
    ) : AiResponse()
}