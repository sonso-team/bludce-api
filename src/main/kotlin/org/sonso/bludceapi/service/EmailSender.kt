package org.sonso.bludceapi.service

interface EmailSender {
    fun sendPassCodeMessage(to: String, passCode: String)
}
