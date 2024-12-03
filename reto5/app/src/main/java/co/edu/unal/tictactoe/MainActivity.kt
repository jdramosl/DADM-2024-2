package co.edu.unal.tictactoe

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
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


    private lateinit var mBoardView: BoardView

    // Texto de información
    private lateinit var mInfoTextView: TextView

    private lateinit var mNewGameButton: Button

    // Variable para alternar el turno inicial
    private var isHumanStarting = true

    private var isHumanTurn = true

    private var humanWinsCount = 0
    private var tiesCount = 0
    private var androidWinsCount = 0

    private var mHumanMediaPlayer: MediaPlayer? = null
    private var mComputerMediaPlayer: MediaPlayer? = null

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



        humanWinsTextView = findViewById(R.id.human_wins)
        tiesTextView = findViewById(R.id.ties)
        androidWinsTextView = findViewById(R.id.android_wins)


        // Inicializar el TextView
        mInfoTextView = findViewById(R.id.information)

        // Inicializar el juego
        mGame = TicTacToeGame()

        // Inicializar el botón de nuevo juego
        mNewGameButton = findViewById(R.id.button_new_game)


        // Inicializar el arreglo de botones
        mBoardView = findViewById(R.id.board)

        mBoardView.setGame(mGame)

        val mTouchListener = View.OnTouchListener { v, event ->

            if (event.action == MotionEvent.ACTION_DOWN && isHumanTurn && !gameOver) {
                // Convertir las coordenadas táctiles a la celda correspondiente

                val col = (event.x / mBoardView.getBoardCellWidth()).toInt()
                val row = (event.y / mBoardView.getBoardCellHeight()).toInt()
                val pos = row * 3 + col // Calculamos la posición en el tablero



                // Verificar que la celda esté vacía y colocar la ficha del jugador
                if (mGame.getBoardOccupant(pos) != TicTacToeGame.OPEN_SPOT) {
                    if (mHumanMediaPlayer != null) {
                        mHumanMediaPlayer?.start() // Reproduce el sonido del humano
                    }
                    // Colocar la ficha del jugador
                    mGame.setMove(TicTacToeGame.HUMAN_PLAYER, pos)
                    mBoardView.invalidate() // Redibujar el tablero

                    // Verificar si hay un ganador
                    var winner = mGame.checkForWinner()
                    if (winner == 0) {
                        // Si no hay ganador, hacer que la computadora juegue
                        isHumanTurn = false
                        // Usar Handler para hacer que la computadora espere 1 segundo
                        Handler().postDelayed({
                            val computerMove = mGame.getComputerMove()
                            setMove(TicTacToeGame.COMPUTER_PLAYER, computerMove)
                            // Para la computadora
                            if (mComputerMediaPlayer != null) {
                                mComputerMediaPlayer?.start() // Reproduce el sonido de la computadora
                            }
                            mInfoTextView.setText(R.string.turn_human)
                            mBoardView.invalidate() // Redibujar el tablero después del movimiento de la computadora
                            isHumanTurn = true
                            winner = mGame.checkForWinner()
                            checkWinner(winner)
                        }, 2500) // 1000 milisegundos = 1 segundo

                    }

                    checkWinner(winner)
                }

                // Asegurarse de que el clic sea registrado por los servicios de accesibilidad
                v.performClick()  // Llamamos a performClick() para la accesibilidad
            }
            false // No estamos interesados en eventos de movimiento o levantamiento de dedo
        }

        mBoardView.setOnTouchListener(mTouchListener)

        // Comenzar un nuevo juego al cargar la app
        startNewGame()

        // Configurar el listener para el botón de nuevo juego
        mNewGameButton.setOnClickListener {
            startNewGame()  // Iniciar un nuevo juego cuando se presiona el botón
        }

    }

    private fun checkWinner(winner: Int) {
        // Si el juego ha terminado, muestra un mensaje
        if (winner != 0) {
            showGameOverMessage(winner)
            // Mostrar mensaje según el resultado del juego
            when (winner) {
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

    // Configura el tablero para un nuevo juego
    private fun startNewGame() {
        // Limpiar el tablero interno del juego
        mGame.clearBoard()

        mBoardView.invalidate();

        gameOver = false  // Reinicia el estado de gameOver

        // Decidir quién empieza
        if (isHumanStarting) {
            mInfoTextView.setText(R.string.turn_human)
        } else {
            mInfoTextView.setText(R.string.turn_computer)
            val move = mGame.getComputerMove()
            setMove(TicTacToeGame.COMPUTER_PLAYER, move)
            mInfoTextView.setText(R.string.turn_human)
        }

        // Alternar el turno inicial para la próxima partida
        isHumanStarting = !isHumanStarting
    }

    private fun showGameOverMessage(winner: Int) {
        val message = when (winner) {
            1 -> "¡Es un empate!" // Empate
            2 -> "¡El jugador X ganó!" // Victoria del jugador humano
            3 -> "¡El jugador O ganó!" // Victoria de la computadora
            else -> "¡Juego terminado!"
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }



    private fun setMove(player: Char, location: Int): Boolean {
        // Realiza el movimiento en el juego
        if (mGame.setMove(player, location)) {
            // Invalida el BoardView para redibujarlo y reflejar el movimiento
            mBoardView.invalidate()  // Esto invoca el método onDraw() del BoardView
            return true
        }
        return false
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

    override fun onResume() {
        super.onResume()
        mHumanMediaPlayer = MediaPlayer.create(applicationContext, R.raw.human) // human.mp3
        mComputerMediaPlayer = MediaPlayer.create(applicationContext, R.raw.computer) // computer.mp3
    }

    override fun onPause() {
        super.onPause()
        mHumanMediaPlayer?.release()
        mComputerMediaPlayer?.release()
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