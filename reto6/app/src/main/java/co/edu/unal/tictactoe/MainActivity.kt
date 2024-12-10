package co.edu.unal.tictactoe

import android.app.AlertDialog
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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

    private lateinit var sharedPreferences: SharedPreferences




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // Asocia el layout XML con la actividad

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("ttt_prefs", MODE_PRIVATE)

        // Inicializar el juego
        mGame = TicTacToeGame()

        // Restaurar el nivel de dificultad
        val difficultyValue = sharedPreferences.getInt("difficultyLevel", DifficultyLevel.Expert.value)
        mGame.difficultyLevel = DifficultyLevel.fromInt(difficultyValue)

        // Restaurar puntajes
        humanWinsCount = sharedPreferences.getInt("humanWins", 0)
        androidWinsCount = sharedPreferences.getInt("androidWins", 0)
        tiesCount = sharedPreferences.getInt("ties", 0)


        humanWinsTextView = findViewById(R.id.human_wins)
        tiesTextView = findViewById(R.id.ties)
        androidWinsTextView = findViewById(R.id.android_wins)


        // Inicializar el TextView
        mInfoTextView = findViewById(R.id.information)



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
                if (mGame.getBoardOccupant(pos) != TicTacToeGame.OPEN_SPOT
                    && mGame.getBoardOccupant(pos) != TicTacToeGame.HUMAN_PLAYER
                    && mGame.getBoardOccupant(pos) != TicTacToeGame.COMPUTER_PLAYER) {
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
                        Handler(Looper.getMainLooper()).postDelayed({
                            val computerMove = mGame.getComputerMove()
                            try {
                                if (mComputerMediaPlayer != null) {
                                    mComputerMediaPlayer?.start()
                                }
                                setMove(TicTacToeGame.COMPUTER_PLAYER, computerMove)
                                mInfoTextView.setText(R.string.turn_human)
                                mBoardView.invalidate() // Redibujar el tablero después del movimiento de la computadora
                                isHumanTurn = true
                                winner = mGame.checkForWinner()
                                checkWinner(winner)

                            } catch (e: Exception) {
                                isHumanTurn = false
                                Log.e("TicTacToe", "Error al ejecutar el movimiento del ordenador", e)
                            }
                        }, 2500)

                    }

                    checkWinner(winner)
                }

                // Asegurarse de que el clic sea registrado por los servicios de accesibilidad
                v.performClick()  // Llamamos a performClick() para la accesibilidad
            } else if(!isHumanTurn){
                Handler(Looper.getMainLooper()).postDelayed({
                    val computerMove = mGame.getComputerMove()
                    try {
                        if (mComputerMediaPlayer != null) {
                            mComputerMediaPlayer?.start() // Reproduce el sonido de la computadora
                        }
                        setMove(TicTacToeGame.COMPUTER_PLAYER, computerMove)
                        mInfoTextView.setText(R.string.turn_human)
                        mBoardView.invalidate() // Redibujar el tablero después del movimiento de la computadora
                        isHumanTurn = true
                        val winner = mGame.checkForWinner()
                        checkWinner(winner)

                    } catch (e: Exception) {
                        isHumanTurn = false
                        Log.e("TicTacToe", "Error al ejecutar el movimiento del ordenador", e)
                    }
                }, 2500)
            }
            false // No estamos interesados en eventos de movimiento o levantamiento de dedo
        }

        mBoardView.setOnTouchListener(mTouchListener)

        if (savedInstanceState == null) {
            // Iniciar un nuevo juego si no hay estado guardado
            startNewGame()
        } else {
            // Restaurar el estado del juego desde el Bundle
            mGame.boardState = savedInstanceState.getCharArray("board")
            gameOver = savedInstanceState.getBoolean("gameOver", false)
            isHumanStarting = savedInstanceState.getBoolean("isHumanStarting", true)
            isHumanTurn = savedInstanceState.getBoolean("isHumanTurn", true)
            humanWinsCount = savedInstanceState.getInt("humanWinsCount", 0)
            tiesCount = savedInstanceState.getInt("tiesCount", 0)
            androidWinsCount = savedInstanceState.getInt("androidWinsCount", 0)
            mInfoTextView.text = savedInstanceState.getCharSequence("info", "")

            if (!gameOver && !isHumanTurn) {
                val computerMove = mGame.getComputerMove()
                setMove(TicTacToeGame.COMPUTER_PLAYER, computerMove)

                mInfoTextView.setText(R.string.turn_human)
                mBoardView.invalidate() // Redibujar el tablero después del movimiento de la computadora
                isHumanTurn = true
            }

        }
        // Mostrar puntajes actualizados
        displayScores()
        // Configurar el listener para el botón de nuevo juego
        mNewGameButton.setOnClickListener {
            startNewGame()  // Iniciar un nuevo juego cuando se presiona el botón
        }

    }

    private fun displayScores() {
        tiesTextView.text = "Ties: $tiesCount"
        humanWinsTextView.text = "Human: $humanWinsCount"
        androidWinsTextView.text = "Android: $androidWinsCount"
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

    override fun onStop() {
        super.onStop()

        // Guardar los puntajes actuales
        sharedPreferences.edit().apply(){
            putInt("difficultyLevel", mGame.difficultyLevel.value)
            putInt("humanWins", humanWinsCount)
            putInt("androidWins", androidWinsCount)
            putInt("ties", tiesCount)
            apply()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // Restaurar el estado del juego desde el Bundle
        mGame.boardState = savedInstanceState.getCharArray("board")
        gameOver = savedInstanceState.getBoolean("gameOver", false)
        isHumanStarting = savedInstanceState.getBoolean("isHumanStarting", true)
        isHumanTurn = savedInstanceState.getBoolean("isHumanTurn", true)
        humanWinsCount = savedInstanceState.getInt("humanWinsCount", 0)
        tiesCount = savedInstanceState.getInt("tiesCount", 0)
        androidWinsCount = savedInstanceState.getInt("androidWinsCount", 0)
        mInfoTextView.text = savedInstanceState.getCharSequence("info", "")

        // Mostrar puntajes actualizados
        displayScores()
    }


    // Configura el tablero para un nuevo juego
    private fun startNewGame() {
        // Limpiar el tablero interno del juego
        mGame.clearBoard()

        mBoardView.invalidate();

        gameOver = false  // Reinicia el estado de gameOver
        isHumanTurn = isHumanStarting
        // Decidir quién empieza
        if (isHumanStarting) {
            mInfoTextView.setText(R.string.turn_human)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                val computerMove = mGame.getComputerMove()
                try {

                    if (mComputerMediaPlayer != null) {
                        mComputerMediaPlayer?.start() // Reproduce el sonido de la computadora
                    }
                    setMove(TicTacToeGame.COMPUTER_PLAYER, computerMove)
                    mInfoTextView.setText(R.string.turn_human)
                    mBoardView.invalidate() // Redibujar el tablero después del movimiento de la computadora
                    isHumanTurn = true
                } catch (e: Exception) {
                    isHumanTurn = false
                    Log.e("TicTacToe", "Error al ejecutar el movimiento del ordenador", e)
                }
            }, 1000) // 1000 milisegundos = 1 segundo
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
            R.id.menu_reset_scores -> {
                humanWinsCount = 0
                androidWinsCount = 0
                tiesCount = 0

                // Guardar los cambios en SharedPreferences
                val editor = sharedPreferences.edit()
                editor.putInt("humanWins", humanWinsCount)
                editor.putInt("androidWins", androidWinsCount)
                editor.putInt("ties", tiesCount)
                editor.apply()

                // Actualizar la visualización
                displayScores()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)


        // Guardar el estado del tablero
        outState.putCharArray("board", mGame.boardState)


        // Guardar variables de estado
        outState.putBoolean("gameOver", gameOver)
        outState.putBoolean("isHumanStarting", isHumanStarting)
        outState.putBoolean("isHumanTurn", isHumanTurn)
        outState.putInt("humanWinsCount", humanWinsCount)
        outState.putInt("tiesCount", tiesCount)
        outState.putInt("androidWinsCount", androidWinsCount)
        outState.putCharSequence("info", mInfoTextView.text)
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