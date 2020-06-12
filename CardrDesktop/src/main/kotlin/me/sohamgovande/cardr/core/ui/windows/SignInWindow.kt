package me.sohamgovande.cardr.core.ui.windows

import com.google.gson.JsonParser
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.GridPane
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.WindowEvent
import me.sohamgovande.cardr.core.auth.CardrUser
import me.sohamgovande.cardr.data.prefs.Prefs
import me.sohamgovande.cardr.data.encryption.EncryptionHelper
import me.sohamgovande.cardr.data.urls.UrlHelper
import me.sohamgovande.cardr.util.OS
import me.sohamgovande.cardr.util.getOSType
import org.apache.logging.log4j.LogManager
import java.awt.Desktop
import java.lang.Exception
import java.net.URL
import kotlin.system.exitProcess


class SignInWindow(private val options: SignInLauncherOptions, private val currentUser: CardrUser) : ModalWindow("Sign in to cardr") {

    private val emailTF = TextField()
    private val passwordTF = PasswordField()
    private val continueBtn = Button("Continue")
    private var readyToClose = false

    override fun close(event: WindowEvent?) {
        super.removeFromList()
        if (forcedClose) {
            super.close(event)
            return
        }
        if (!readyToClose && options != SignInLauncherOptions.MANUAL_SIGNIN) {
            exitProcess(0)
        }
    }

    private fun loadPassword() {
        if (Prefs.get().encryptedPassword == "")
            return

        emailTF.text = "Loading..."
        emailTF.isEditable = false
        passwordTF.isEditable = false

        Thread {
            val passwordEncrypted = Prefs.get().encryptedPassword
            val encryptor = EncryptionHelper(EncryptionHelper.getEncryptionInfo())
            Platform.runLater {
                emailTF.text = Prefs.get().emailAddress
                emailTF.isEditable = true
                passwordTF.isEditable = true

                try {
                    passwordTF.text = encryptor.decrypt(passwordEncrypted)
                } catch (e: Exception) {
                    logger.error("Unable to decrypt password", e)
                }
            }
        }.start()
    }

    private fun onClickContinueBtn(@Suppress("UNUSED_PARAMETER") e: ActionEvent) {
        continueBtn.text = "Processing..."
        continueBtn.isDisable = true
        Thread {
            val result = currentUser.login(emailTF.text, passwordTF.text)
            Platform.runLater {
                continueBtn.text = "Continue"
                continueBtn.isDisable = false
                if (result.wasSuccessful()) {
                    readyToClose = true
                    super.window.close()
                } else {
                    val alert = Alert(AlertType.ERROR)
                    alert.dialogPane.minHeight = Region.USE_PREF_SIZE
                    alert.title = "Error"
                    if (result.reason == "That username and password combination is invalid.") {
                        alert.headerText = "Invalid email and password combination."
                        alert.contentText = "To reset your password, please visit https://cardrdebate.com/forgot-password-instructions.html."
                    } else if (result.additional_info.contains("custom_dialog")) {
                        val jsonData = JsonParser().parse(result.reason).asJsonObject
                        alert.headerText = if (jsonData.has("header")) jsonData["header"].asString else "No message header"
                        alert.contentText = if (jsonData.has("header")) jsonData["body"].asString else "No message body"
                    } else {
                        alert.headerText = "Login error: ${result.reason}"
                        alert.contentText = "An error occurred while logging in. \n\n" +
                            "Here's what we know—\n" +
                            "Performing action: ${result.func}\n" +
                            "Status: ${result.status}\n" +
                            "Reason: ${result.reason}\n" +
                            "Additional info: ${result.additional_info}"
                    }

                    alert.showAndWait()
                }
            }
        }.start()
    }

    private fun generateStatusMessage(): String {
        return when (options) {
            SignInLauncherOptions.WELCOME -> "Please sign in below to continue."
            SignInLauncherOptions.TOKEN_EXPIRED -> "Your access token has expired. Please sign in again."
            SignInLauncherOptions.MANUAL_SIGNIN -> "Please sign in to cardr."
        }
    }

    override fun generateUI(): Scene {
        val vbox = VBox()
        vbox.spacing = 5.0
        vbox.padding = Insets(10.0)

        loadPassword()

        val gp = GridPane()
        gp.hgap = 5.0
        gp.vgap = 5.0
        gp.padding = Insets(10.0)

        emailTF.promptText = "Email"
        emailTF.prefColumnCount = 20
        passwordTF.promptText = "Password"
        passwordTF.prefColumnCount = 20

        passwordTF.setOnKeyPressed {
            if (it.code == KeyCode.ENTER) {
                continueBtn.fire()
            }
            if (getOSType() == OS.MAC) {
                if (passwordTF.text.isNotEmpty()) {
                    passwordTF.font = Font.font(8.0)
                } else {
                    passwordTF.font = Font.getDefault()
                }
            }
        }
        emailTF.setOnKeyPressed {
            if (it.code == KeyCode.ENTER) {
                continueBtn.fire()
            }
        }

        gp.add(emailTF, 1, 0)
        gp.add(passwordTF, 1, 1)

        val header = Label("Sign in to cardr")
        if (options == SignInLauncherOptions.WELCOME) {
            header.text = "Welcome to cardr!"
        }

        header.font = Font.font(20.0)
        header.style = "-fx-font-family: 'Calibri';"
        val subheader = Label(generateStatusMessage())
        val dontHaveAccount = Label("Don't have an account?")
        dontHaveAccount.style = "-fx-cursor: hand;"
        dontHaveAccount.textFill = Color.BLUE
        dontHaveAccount.setOnMouseClicked {
            UrlHelper.browse("signUp")
        }


        val forgotPassword = Label("Forgot password?")
        forgotPassword.style = "-fx-cursor: hand;"
        forgotPassword.textFill = Color.BLUE
        forgotPassword.setOnMouseClicked {
            UrlHelper.browse("forgotPassword")
        }

        val headerCardBody = Label("Card Body")
        headerCardBody.style = "-fx-font-weight: bold;"
        val headerTagAndCite = Label("Tag and Cite")
        headerTagAndCite.style = "-fx-font-weight: bold;"

        continueBtn.prefWidth = 300.0
        continueBtn.setOnAction(this::onClickContinueBtn)

        vbox.children.add(header)
        vbox.children.add(subheader)
        vbox.children.add(dontHaveAccount)
        vbox.children.add(forgotPassword)
        vbox.children.add(gp)
        vbox.children.add(continueBtn)

        val scene = Scene(vbox, 300.0, 220.0)
        scene.stylesheets.add(javaClass.getResource(Prefs.get().getStylesheet()).toExternalForm())
        super.window.icons.add(Image(javaClass.getResourceAsStream("/icon-128.png")))

        Platform.runLater {
            val height = passwordTF.height
            passwordTF.prefHeight = height
            passwordTF.minHeight = height
        }
        return scene
    }

    companion object {
        val logger = LogManager.getLogger(SignInWindow::class.java)
    }

}
