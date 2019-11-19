package com.mcc.g22

class User(val username: String) {

    companion object {

        /**
         * Return user registered on this device
         */
        fun getRegisteredUser(): User? {
            return User("test user")
        }
    }
}
