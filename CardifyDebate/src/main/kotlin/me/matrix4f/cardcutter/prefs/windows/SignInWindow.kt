package me.matrix4f.cardcutter.prefs.windows

import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.StageStyle
import javafx.stage.WindowEvent
import me.matrix4f.cardcutter.auth.CardifyUser
import me.matrix4f.cardcutter.prefs.Prefs
import java.awt.Desktop
import java.net.URL


class SignInWindow(private val options: SignInLauncherOptions, private val currentUser: CardifyUser) : ModalWindow("Sign in to Cardify") {

    private val emailTF = TextField()
    private val passwordTF = PasswordField()
    private val continueBtn = Button("Continue")
    private var readyToClose = false

    init {
        window.initStyle(StageStyle.UTILITY)
    }

    override fun close(event: WindowEvent?) {
        if (!readyToClose && (Prefs.get().emailAddress.isEmpty() || Prefs.get().accessToken.isEmpty())) {
            System.exit(0)
            event?.consume()
        }
    }

    private fun onClickContinueBtn(e: ActionEvent) {
        continueBtn.text = "Processing..."
        continueBtn.isDisable = true
        Thread {
            val result = currentUser.login(emailTF.text, passwordTF.text)
            Platform.runLater {
                continueBtn.text = "Continue"
                continueBtn.isDisable = false
                if (result.wasSuccessful()) {
                    readyToClose = true;
                    super.window.close()

                    val alert = Alert(AlertType.INFORMATION)
                    alert.title = "Success"
                    alert.headerText = "Sucessfully logged in"
                    alert.contentText = "You may continue to use Cardify."


                    alert.showAndWait()
                } else {
                    val alert = Alert(AlertType.ERROR)
                    alert.title = "Error"
                    alert.headerText = "Login error: ${result.reason}"
                    alert.contentText = "An error occurred while logging in. \n\n" +
                        "Here's what we know—\n" +
                        "Performing action: ${result.func}\n" +
                        "Status: ${result.status}\n" +
                        "Reason: ${result.reason}\n" +
                        "Additional info: ${result.additional_info}"

                    alert.showAndWait()
                }
            }
        }.start()
    }

    private fun generateStatusMessage(): String {
        return when (options) {
            SignInLauncherOptions.WELCOME -> "Please sign in below to continue."
            SignInLauncherOptions.TOKEN_EXPIRED -> "Your access token has expired. Please sign in again."
            SignInLauncherOptions.MANUAL_SIGNIN -> ""
        }
    }

    override fun generateUI(): Scene {
        val vbox = VBox()
        vbox.style = "-fx-background-color:#f4f4f4;"
        vbox.spacing = 5.0
        vbox.padding = Insets(10.0)

        val gp = GridPane()
        gp.hgap = 5.0
        gp.vgap = 5.0
        gp.padding = Insets(10.0)

        emailTF.promptText = "Email"
        emailTF.prefColumnCount = 60
        passwordTF.promptText = "Password"
        passwordTF.prefColumnCount = 60

        gp.add(emailTF, 1, 0)
        gp.add(passwordTF, 1, 1)

        val header = Label("Sign in to Cardify")
        if (options == SignInLauncherOptions.WELCOME) {
            header.text = "Welcome to Cardify!"
        }

        header.font = Font.font(20.0)
        header.style = "-fx-font-family: 'Calibri';"
        val subheader = Label(generateStatusMessage())
        val dontHaveAccount = Label("Don't have an account?")
        dontHaveAccount.style = "-fx-cursor: hand;"
        dontHaveAccount.setTextFill(Color.BLUE)
        dontHaveAccount.setOnMouseClicked {
            Desktop.getDesktop().browse(URL("http://cardifydebate.x10.bz/sign-up.html").toURI())
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
        vbox.children.add(gp)
        vbox.children.add(continueBtn)

        val scene = Scene(vbox, 300.0, 200.0)
        scene.stylesheets.add(javaClass.getResource("/styles.css").toExternalForm())
        return scene
    }

}