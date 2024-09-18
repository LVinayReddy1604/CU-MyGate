package com.example.cu

import java.io.File
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart

object EmailSender {
    private const val username = "pratham.m@mca.christuniversity.in"
    private const val password = "Prathammanju@99"

    private val props = System.getProperties().apply {
        put("mail.smtp.host", "smtp.example.com")
        put("mail.smtp.auth", "true")
        put("mail.smtp.port", "587")
    }

    private val session: Session = Session.getInstance(props, object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(username, password)
        }
    })

    fun sendEmail(recipientEmail: String, subject: String, message: String): Boolean {
        return try {
            val mimeMessage = MimeMessage(session)
            mimeMessage.setFrom(InternetAddress(username))
            mimeMessage.addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
            mimeMessage.subject = subject
            mimeMessage.setText(message)

            Transport.send(mimeMessage)
            true
        } catch (e: MessagingException) {
            e.printStackTrace()
            false
        }
    }

    fun sendEmailWithQRCode(recipientEmail: String, qrCodeFile: File): Boolean {
        return try {
            val mimeMessage = MimeMessage(session)
            mimeMessage.setFrom(InternetAddress(username))
            mimeMessage.addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
            mimeMessage.subject = "Your QR Code for University Visit"

            val messageBodyPart = MimeBodyPart()
            messageBodyPart.setText("Dear Visitor,\n\nPlease find your QR code attached.\n\nBest regards,\nUniversity Security Team")

            val attachmentBodyPart = MimeBodyPart()
            attachmentBodyPart.attachFile(qrCodeFile)

            val multipart = MimeMultipart().apply {
                addBodyPart(messageBodyPart)
                addBodyPart(attachmentBodyPart)
            }

            mimeMessage.setContent(multipart)

            Transport.send(mimeMessage)
            true
        } catch (e: MessagingException) {
            e.printStackTrace()
            false
        }
    }
}
