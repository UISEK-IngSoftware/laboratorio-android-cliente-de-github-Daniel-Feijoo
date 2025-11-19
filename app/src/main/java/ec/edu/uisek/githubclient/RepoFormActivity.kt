package ec.edu.uisek.githubclient

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityRepoFormBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoRequest
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepoFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRepoFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.saveButton.setOnClickListener {
            val name = binding.repoNameInput.text.toString().trim()
            val description = binding.repoDescriptionInput.text.toString().trim()
            val language = binding.repoLanguageInput.text.toString().trim()

            if (name.isEmpty()) {
                showMessage("El nombre del repositorio es obligatorio")
                return@setOnClickListener
            }

            val request = RepoRequest(name, description, language)

            RetrofitClient.gitHubApiService.createRepo(request).enqueue(object : Callback<Repo> {
                override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                    if (response.isSuccessful) {
                        val newRepo = response.body()
                        if (newRepo != null) {
                            val resultIntent = Intent()
                            resultIntent.putExtra("newRepo", newRepo)
                            setResult(RESULT_OK, resultIntent)
                            showMessage("Repositorio creado en GitHub")
                            finish()
                        } else {
                            showMessage("Respuesta vacía del servidor")
                        }
                    } else {
                        showMessage("Error al crear: ${response.code()} - ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<Repo>, t: Throwable) {
                    showMessage("Error de red: ${t.message}")
                }
            })
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}