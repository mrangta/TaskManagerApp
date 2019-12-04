/**
* mcc-g22-api
* Api for the mcc course group 22
*
* The version of the OpenAPI document: 1
* 
*
* NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
* https://openapi-generator.tech
* Do not edit the class manually.
*/
package com.mcc.g22.apiclient.models


import com.squareup.moshi.Json
/**
 * 
 * @param description 
 * @param status 
 * @param deadline 
 */

data class Task (
    @Json(name = "description")
    val description: kotlin.String,
    @Json(name = "status")
    val status: Task.Status,
    @Json(name = "deadline")
    val deadline: kotlin.String
) 


{
    /**
    * 
    * Values: pending,ongoing,completed
    */
    
    enum class Status(val value: kotlin.String){
        @Json(name = "pending") pending("pending"),
        @Json(name = "ongoing") ongoing("ongoing"),
        @Json(name = "completed") completed("completed");
    }
}

