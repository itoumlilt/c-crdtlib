package crdtlib.utils

import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
* This class represents a datacenter (DC) identifier (id).
* @property name the name associated with the DC.
**/
@Serializable
data class DCId(val name: String) : Comparable<DCId> {

    /**
    * Compares this DC name to a given other datacenter name.
    * @param other the other instance of datacenter id.
    * @return the results of the comparison between the two DC name.
    **/
    override fun compareTo(other: DCId): Int {
        return this.name.compareTo(other.name)
    }

    fun toJson(): String {
        val JSON = Json(JsonConfiguration.Stable)
        return JSON.stringify(DCId.serializer(), this)
    }

    companion object {
        fun fromJson(json: String): DCId {
            val JSON = Json(JsonConfiguration.Stable)
            return JSON.parse(DCId.serializer(), json)
        }
    }
}
