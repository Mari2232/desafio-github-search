package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.data.NetworkUtils
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

private const val USER_KEY = "user_salvo"

class MainActivity : AppCompatActivity() {

    lateinit var nomeUsuario: EditText
    lateinit var btnConfirmar: Button
    lateinit var listaRepositories: RecyclerView
    lateinit var githubApi: GitHubService
    lateinit var retrofitClient: Retrofit
    lateinit var repositoryAdapter: RepositoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        showUserName()
        setupRetrofit()
        getAllReposByUserName()
    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    fun setupView() {
        //@TODO 1 - okRecuperar os Id's da tela para a Activity com o findViewById
        nomeUsuario = findViewById(R.id.et_nome_usuario)
        btnConfirmar = findViewById(R.id.btn_confirmar)
        listaRepositories = findViewById(R.id.rv_lista_repositories)
        setupListeners()
    }

    //metodo responsavel por configurar os listeners click da tela
    //@TODO 2 - okcolocar a acao de click do botao confirmar
    private fun setupListeners() {
        btnConfirmar.setOnClickListener {
            val nomeUser = nomeUsuario.text.toString()
            saveUserLocal(nomeUser)
            val callback = githubApi.getAllRepositoriesByUser(nomeUser)
            callback.enqueue(object : Callback<List<Repository>> {
                override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                    Toast.makeText(baseContext, t.message, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(
                    call: Call<List<Repository>>,
                    response: Response<List<Repository>>
                ) {
                    response.body()?.let {
                        setupAdapter(it)
                    }
                }
            })
        }
    }

    // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    //@TODO 3 - okPersistir o usuario preenchido na editText com a SharedPref no listener do botao salvar
    private fun saveUserLocal(user: String) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(USER_KEY, user)
            apply()
        }
    }

    //@TODO 4- okdepois de persistir o usuario exibir sempre as informacoes no EditText  se a sharedpref possuir algum valor, exibir no proprio editText o valor salvo
    private fun showUserName() {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        val currentUser = sharedPref.getString(USER_KEY, null)
        if (currentUser != null) {
            nomeUsuario.setText(currentUser)
        }
    }

    //Metodo responsavel por fazer a configuracao base do Retrofit
    /* @TODO 5 -  okrealizar a Configuracao base do retrofit
           Documentacao oficial do retrofit - https://square.github.io/retrofit/
           URL_BASE da API do  GitHub= https://api.github.com/
           lembre-se de utilizar o GsonConverterFactory mostrado no curso */

    private fun setupRetrofit() {
        retrofitClient = NetworkUtils
            .getRetrofitInstance()
        githubApi = retrofitClient.create(GitHubService::class.java)
    }

    //Metodo responsavel por buscar todos os repositorios do usuario fornecido
    // TODO 6 - okrealizar a implementacao do callback do retrofit e chamar o metodo setupAdapter se retornar os dados com sucesso

    private fun getAllReposByUserName() {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        val currentUser = sharedPref.getString(USER_KEY, null)
        if (currentUser != null) {
            val callback = githubApi.getAllRepositoriesByUser(currentUser)
            callback.enqueue(object : Callback<List<Repository>> {
                override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                    Toast.makeText(baseContext, t.message, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(
                    call: Call<List<Repository>>,
                    response: Response<List<Repository>>
                ) {
                    response.body()?.let {
                        setupAdapter(it)
                    }
                }
            })
        }
    }


    // Metodo responsavel por realizar a configuracao do adapter
    /*
            @TODO 7 - okImplementar a configuracao do Adapter , construir o adapter e instancia-lo
            passando a listagem dos repositorios
         */
    fun setupAdapter(list: List<Repository>) {
        repositoryAdapter = RepositoryAdapter(list)
        repositoryAdapter.carItemLister = {
            openBrowser(it.htmlUrl)
        }
        repositoryAdapter.btnShareLister = { repository ->
            shareRepositoryLink(repository.htmlUrl)
        }
        listaRepositories.adapter = repositoryAdapter
    }


    // Metodo responsavel por compartilhar o link do repositorio selecionado
    // @Todo 11 - okColocar esse metodo no click do share item do adapter
    private fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Metodo responsavel por abrir o browser com o link informado do repositorio

    // @Todo 12 - okColocar esse metodo no click item do adapter
    private fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )
    }

}