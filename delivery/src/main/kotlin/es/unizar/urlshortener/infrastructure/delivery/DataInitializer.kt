package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.User
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.boot.CommandLineRunner
import es.unizar.urlshortener.core.usecases.GetUserInformationUseCase
import java.io.File

@Profile("dev-sync", "dev-async") // Solo se ejecutará en los perfiles dev-sync y dev-async
@Component
class DataInitializer(
    private val getUserInformationUseCase: GetUserInformationUseCase
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        val filePath = javaClass.classLoader.getResource("usersId.csv")?.file
        val users = mutableListOf<User>()

        println("INICIALZANDO USUARIOS PARA TEST DESDE CSV...")

        // Leer el archivo CSV
        File(filePath).useLines { lines ->
            lines.drop(1) // Saltar la cabecera
                .forEach { line ->
                    val userId = line.trim() // Extraer el ID del usuario
                    if (userId.isNotBlank()) {
                        // Crear usuario con solo el ID
                        System.out.println("Anyadiendo usuario con ID: " + userId)
                        users.add(
                            User(
                                userId = userId,
                                redirections = 0,
                                lastRedirectionTimeStamp = null
                            )
                        )
                    }
                }
        }

        // Guardar los usuarios en la base de datos
        users.forEach { user ->
            getUserInformationUseCase.processUser(user)
        }

        println("Users initialized successfully from CSV!")
    }
}
