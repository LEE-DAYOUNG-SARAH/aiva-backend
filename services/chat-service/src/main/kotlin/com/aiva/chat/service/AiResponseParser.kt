package com.aiva.chat.service

import com.aiva.chat.entity.MessageSource
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.util.*

@Component
class AiResponseParser(private val objectMapper: ObjectMapper) {
    
    fun parseEventLine(line: String): AiEvent? {
        return try {
            when {
                line.startsWith("event: start") -> AiEvent.Start
                line.startsWith("event: chunk") -> AiEvent.Chunk
                line.startsWith("event: metadata") -> AiEvent.Metadata
                line.startsWith("event: done") -> AiEvent.Done
                line.startsWith("event: error") -> AiEvent.Error
                line.startsWith("data: ") && !line.contains("event:") -> {
                    val json = line.removePrefix("data: ")
                    val node = objectMapper.readTree(json)
                    AiEvent.Data(node)
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun extractLogId(dataNode: JsonNode): String? {
        return dataNode.get("log_id")?.asText()
    }
    
    fun extractDelta(dataNode: JsonNode): String? {
        return dataNode.get("delta")?.asText()
    }
    
    fun extractSources(dataNode: JsonNode): List<MessageSource> {
        val sources = mutableListOf<MessageSource>()
        
        dataNode.get("sources")?.let { sourcesArray ->
            sourcesArray.forEach { sourceNode ->
                sources.add(
                    MessageSource(
                        messageId = UUID.randomUUID(), // 임시, 저장시 실제 ID 설정
                        title = sourceNode.get("doc")?.asText() ?: "",
                        link = sourceNode.get("link")?.asText()
                    )
                )
            }
        }
        
        return sources
    }
}

sealed class AiEvent {
    object Start : AiEvent()
    object Chunk : AiEvent()
    object Metadata : AiEvent()
    object Done : AiEvent()
    object Error : AiEvent()
    data class Data(val content: JsonNode) : AiEvent()
}