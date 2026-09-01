import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.createSupabaseClient
import kotlinx.serialization.Serializable

@Serializable
data class Args(val a: String)

fun main() {
    val client = createSupabaseClient("", "") {
        install(io.github.jan.supabase.postgrest.Postgrest)
    }
    client.postgrest.rpc("my_func", Args("b"))
}
