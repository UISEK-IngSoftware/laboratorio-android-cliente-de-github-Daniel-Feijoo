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

class EditRepoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRepoFormBinding
    private lateinit var originalRepo: Repo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        originalRepo = intent.getSerializableExtra("repo") as Repo

        binding.repoNameInput.setText(originalRepo.name)
        binding.repoDescriptionInput.setText(originalRepo.description)
        binding.repoLanguageInput.setText(originalRepo.language)

        binding.saveButton.setOnClickListener {
            val newName = binding.repoNameInput.text.toString()
            val newDescription = binding.repoDescriptionInput.text.toString()
            val newLanguage = binding.repoLanguageInput.text.toString()

            val repoRequest = RepoRequest(
                name = newName,
                description = newDescription,
                language = newLanguage
            )

            RetrofitClient.gitHubApiService.updateRepo(
                owner = originalRepo.owner.login,
                repoName = originalRepo.name,
                repoRequest = repoRequest
            ).enqueue(object : Callback<Repo> {
                override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                    if (response.isSuccessful) {
                        val updatedRepo = response.body()
                        if (updatedRepo != null) {
                            val resultIntent = Intent()
                            resultIntent.putExtra("updatedRepo", updatedRepo)
                            setResult(RESULT_OK, resultIntent)
                            showMessage("Repositorio actualizado en GitHub")
                            finish()
                        }
                    } else {
                        showMessage("Error al actualizar: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Repo>, t: Throwable) {
                    showMessage("Error de red al actualizar")
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