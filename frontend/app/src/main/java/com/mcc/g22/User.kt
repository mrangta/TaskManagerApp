package com.mcc.g22

class User {
    var username: String = ""
        get
        private set

    companion object {

        /**
         * Return user registered on this device
         */
        fun getRegisteredUser(): User? {
            return User()
        }
    }
}