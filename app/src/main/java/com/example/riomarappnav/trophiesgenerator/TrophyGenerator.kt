package com.example.riomarappnav.trophiesgenerator

import com.example.riomarappnav.database.FirestoreRepository

class TrophyGenerator(private val firestoreRepository: FirestoreRepository) {
    private val constanteTrofeus = 10

    fun gerenciarTrofeus(nomeUser: String, listasDeClasses: List<String>, onResult: (Boolean) -> Unit) {
        firestoreRepository.verificarDocumentoUsuario { existe ->
            if (existe) {
                // Se o documento existe, incrementa os troféus
                val novosTrofeus = listasDeClasses.size * constanteTrofeus
                firestoreRepository.incrementarTrofeus(novosTrofeus) { sucesso ->
                    onResult(sucesso)
                }
            } else {
                // Se o documento não existe, cria com pontuação inicial 0
                firestoreRepository.salvarTrofeusUsuario(0, nomeUser)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Após criar, incrementa os troféus iniciais
                        val novosTrofeus = listasDeClasses.size * constanteTrofeus
                        firestoreRepository.incrementarTrofeus(novosTrofeus) { sucesso ->
                            onResult(sucesso)
                        }
                    } else {
                        onResult(false) // Falha ao criar o documento inicial
                    }
                }
            }
        }
    }

}


