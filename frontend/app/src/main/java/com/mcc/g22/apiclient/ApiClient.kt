package com.mcc.g22.apiclient

import com.mcc.g22.apiclient.apis.DefaultApi

/**
 * Class to keep only one instance of the client
 */
class ApiClient {
    companion object {
        val api: DefaultApi = DefaultApi()
    }
}