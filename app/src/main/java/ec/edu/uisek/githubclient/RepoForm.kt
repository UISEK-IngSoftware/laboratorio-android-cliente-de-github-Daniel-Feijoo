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

class RepoForm : AppCompatActivity() {
    private lateinit var binding: ActivityRepoFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRepoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cancelButton.setOnClickListener { finish() }
        binding.saveButton.setOnClickListener { createRepo() }
    }

    private fun validationForm(): Boolean {
        val repoName = binding.repoNameInput.text.toString()
        val repoLanguage = binding.repoLanguageInput.text.toString()

        var isValid = true

        if (repoName.isBlank()) {
            binding.repoNameInput.error = "El nombre no puede estar vacío."
            isValid = false
        } else {
            binding.repoNameInput.error = null
        }

        if (repoLanguage.isBlank()) {
            binding.repoLanguageInput.error = "El lenguaje no puede estar vacío."
            isValid = false
        } else {
            binding.repoLanguageInput.error = null
        }

        return isValid
    }

    private fun createRepo() {
        if (!validationForm()) return

        val repoName = binding.repoNameInput.text.toString().trim()
        val repoDescription = binding.repoDescriptionInput.text.toString().trim()
        val repoLanguage = binding.repoLanguageInput.text.toString().trim()

        val repoRequest = RepoRequest(
            name = repoName,
            description = repoDescription,
            language = repoLanguage,
            private = false
        )

        RetrofitClient.getApiService()?.createRepo(repoRequest)
            ?.enqueue(object : Callback<Repo> {
                override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                    if (response.isSuccessful) {
                        val createdRepo = response.body()
                        if (createdRepo != null) {
                            val resultIntent = Intent()
                            resultIntent.putExtra("newRepo", createdRepo)
                            setResult(RESULT_OK, resultIntent)
                            showMessage("Repositorio creado en GitHub")
                            finish()
                        }
                    } else {
                        showMessage("Error al crear: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Repo>, t: Throwable) {
                    showMessage("Error de red al crear repositorio")
                }
            })
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}