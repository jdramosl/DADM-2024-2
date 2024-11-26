package co.edu.unal.tictactoe

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import co.edu.unal.tictactoe.TicTacToeGame.DifficultyLevel
import co.edu.unal.tictactoe.ui.theme.AndroidTicTacToeTheme

class MainActivity : ComponentActivity() {
    private lateinit var mGame: TicTacToeGame
    // Arreglo de botones que conforman el tablero
    private lateinit var mBoardButtons: Array<Button>

    // Texto de información
    private lateinit var mInfoTextView: TextView

    private lateinit var mNewGameButton: Button

    // Variable para alternar el turno inicial
    private var isHumanTurn = true

    private var humanWinsCount = 0
    private var tiesCount = 0
    private var androidWinsCount = 0

    private lateinit var humanWinsTextView: TextView
    private lateinit var tiesTextView: TextView
    private lateinit var androidWinsTextView: TextView

    companion object {
        const val DIALOG_DIFFICULTY_ID = 0
        const val DIALOG_QUIT_ID = 1
    }

    private var gameOver = false  // Variable para rastrear si el juego ha terminado



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // Asocia el layout XML con la actividad

        // Inicializar el arreglo de botones
        mBoardButtons = arrayOf(
            findViewById(R.id.one),
            findViewById(R.id.two),
            findViewById(R.id.three),
            findViewById(R.id.four),
            findViewById(R.id.five),
            findViewById(R.id.six),
            findViewById(R.id.seven),
            findViewById(R.id.eight),
            findViewById(R.id.nine)
        )

        humanWinsTextView = findViewById(R.id.human_wins)
        tiesTextView = findViewById(R.id.ties)
        androidWinsTextView = findViewById(R.id.android_wins)


        // Inicializar el TextView
        mInfoTextView = findViewById(R.id.information)

        // Inicializar el juego
        mGame = TicTacToeGame()

        // Inicializar el botón de nuevo juego
        mNewGameButton = findViewById(R.id.button_new_game)

        // Comenzar un nuevo juego al cargar la app
        startNewGame()

        // Configurar el listener para el botón de nuevo juego
        mNewGameButton.setOnClickListener {
            startNewGame()  // Iniciar un nuevo juego cuando se presiona el botón
        }
    }

    // Configura el tablero para un nuevo juego
    private fun startNewGame() {
        // Limpiar el tablero interno del juego
        mGame.clearBoard()

        gameOver = false  // Reinicia el estado de gameOver

        // Restablecer los botones de la interfaz de usuario
        for (i in mBoardButtons.indices) {
            mBoardButtons[i].text = ""          // Limpiar el texto
            mBoardButtons[i].isEnabled = true   // Habilitar el botón
            mBoardButtons[i].setOnClickListener(ButtonClickListener(i)) // Asignar listener
        }

        // Decidir quién empieza
        if (isHumanTurn) {
            mInfoTextView.setText(R.string.turn_human)
        } else {
            mInfoTextView.setText(R.string.turn_computer)
            val move = mGame.getComputerMove()
            setMove(TicTacToeGame.COMPUTER_PLAYER, move)
            mInfoTextView.setText(R.string.turn_human)
        }

        // Alternar el turno inicial para la próxima partida
        isHumanTurn = !isHumanTurn
    }

    // Clase interna para manejar los clics de los botones del tablero
    private inner class ButtonClickListener(val location: Int) : View.OnClickListener {
        override fun onClick(view: View?) {
            if (mBoardButtons[location].isEnabled  && !gameOver) {
                setMove(TicTacToeGame.HUMAN_PLAYER, location)

                // Revisar si hay un ganador después del movimiento humano
                var winner = mGame.checkForWinner()
                if (winner == 0) {
                    mInfoTextView.setText(R.string.turn_human)
                    val move = mGame.getComputerMove()
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                    winner = mGame.checkForWinner()
                }

                // Mostrar mensaje según el resultado del juego
                when (winner) {
                    0 -> mInfoTextView.setText(R.string.turn_human) // No hay ganador, sigue el turno
                    1 -> {
                        tiesCount++
                        tiesTextView.text = "Ties: $tiesCount"
                        mInfoTextView.setText(R.string.result_tie)
                        gameOver = true
                    }
                    2 -> {
                        humanWinsCount++
                        humanWinsTextView.text = "Human: $humanWinsCount"
                        mInfoTextView.setText(R.string.result_human_wins)
                        gameOver = true
                    }
                    else -> {
                        androidWinsCount++
                        androidWinsTextView.text = "Android: $androidWinsCount"
                        mInfoTextView.setText(R.string.result_computer_wins)
                        gameOver = true
                    }
                }

            }
        }
    }

    // Método para realizar el movimiento en el tablero
    private fun setMove(player: Char, location: Int): Int  {
        mGame.setMove(player, location)
        mBoardButtons[location].apply {
            isEnabled = false
            text = player.toString()
            setTextColor(
                if (player == TicTacToeGame.HUMAN_PLAYER) Color.rgb(0, 200, 0)
                else Color.rgb(200, 0, 0)
            )
        }
        return mGame.checkForWinner()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {;
        // Infla el menú desde el archivo XML
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.new_game -> {  // Acción para el ítem de menú "New Game"
                startNewGame()  // Llama a la función para iniciar un nuevo juego
                return true
            }
            R.id.ai_difficulty -> {
                showDifficultyDialog()
                return true
            }
            R.id.quit -> {
                showQuitConfirmationDialog()
                return true
            }
            R.id.menu_about -> {
                showAboutDialog()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showAboutDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.about_dialog, null)
        builder.setView(layout)
        builder.setPositiveButton("OK", null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun showDifficultyDialog() {
        // Opciones de dificultad
        val levels = arrayOf(
            getString(R.string.difficulty_easy),
            getString(R.string.difficulty_harder),
            getString(R.string.difficulty_expert)
        )

        // Determinar el nivel actual para marcarlo como seleccionado
        val selected = when (mGame.difficultyLevel) {
            DifficultyLevel.Easy -> 0
            DifficultyLevel.Harder -> 1
            DifficultyLevel.Expert -> 2
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.difficulty_choose))
            .setSingleChoiceItems(levels, selected) { dialog, item ->
                // Actualizar el nivel de dificultad basado en la selección del usuario
                mGame.difficultyLevel = when (item) {
                    0 -> DifficultyLevel.Easy
                    1 -> DifficultyLevel.Harder
                    2 -> DifficultyLevel.Expert
                    else -> DifficultyLevel.Easy
                }
                // Mostrar mensaje con la selección
                Toast.makeText(this, levels[item], Toast.LENGTH_SHORT).show()
                dialog.dismiss() // Cerrar el diálogo
            }
            .setNegativeButton(getString(R.string.cancel), null) // Botón de cancelar
            .show()
    }


    private fun showQuitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.quit_question))
            .setCancelable(false) // Evita que el usuario cierre el diálogo tocando fuera de él
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                finish() // Finaliza la actividad
            }
            .setNegativeButton(getString(R.string.no), null) // Cierra el diálogo sin hacer nada
            .show()
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidTicTacToeTheme {
        Greeting("Android")
    }
}