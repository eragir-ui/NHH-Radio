package com.example.data

data class RadioStation(
    val id: Int,
    val name: String,
    val streamUrl: String,
    val genre: String,
    val initials: String, // Two letters to display beautifully
    val isHls: Boolean = false,
    val gradientStart: Long,
    val gradientEnd: Long,
    val logoUrl: String? = null
)

object RadioStationRepository {
    val genres = listOf("Tümü")

    val stations = emptyList<RadioStation>()

    // Dynamic Simulated Songs based on Radio Station characteristics
    private val popSongs = listOf(
        "Tarkan - Şımarık",
        "Hadise - Feryat",
        "Murat Boz - Özledim",
        "Edis - Martılar",
        "Zeynep Bastık - Lan",
        "Duman - Senden Daha Güzel",
        "Sertab Erener - Rengarenk",
        "Kenan Doğulu - Güzeller İçinden",
        "Hande Yener - Kırmızı",
        "Mabel Matiz - Sarışın",
        "Tarkan - Dudu",
        "Simge - Aşkın Olayım",
        "Gülşen - Lolipop",
        "KÖFN - Bir Tek Ben Anlarım"
    )

    private val slowSongs = listOf(
        "Sezen Aksu - Vazgeçtim",
        "Cem Adrian - Kül",
        "Model - Pembe Mezarlık",
        "Toygar Işıklı - Hayat Gibi",
        "Şebnem Ferah - Sil Baştan",
        "Teoman - Paramparça",
        "Pinhani - Dünyadan Uzak",
        "Yalın - Ki Sen",
        "Sıla - Acısa da Öldürmez",
        "Sertab Erener - Olsun",
        "Göksel - Gittiğinde",
        "Can Bonomo - Kal Bugün"
    )

    private val arabeskSongs = listOf(
        "Müslüm Gürses - Affet",
        "Orhan Gencebay - Batsın Bu Dünya",
        "Ferdi Tayfur - Huzurum Kalmadı",
        "İbrahim Tatlıses - Aramam",
        "Cengiz Kurtoğlu - Küllenen Aşk",
        "Azer Bülbül - Duygularım",
        "Müslüm Gürses - Seni Yazdım",
        "Bergen - Acıların Kadını",
        "Hakan Altun - Telefonun Başında",
        "Kibariye - Sil Baştan",
        "Ebru Gündeş - Demir Attım"
    )

    private val halkSongs = listOf(
        "Neşet Ertaş - Ah Yalan Dünya",
        "Aşık Veysel - Kara Toprak",
        "Erkan Oğur - Pencereden Kar Geliyor",
        "Volkan Konak - Cerrahpaşa",
        "Selda Bağcan - Gesi Bağları",
        "Kardeş Türküler - Leylim Ley",
        "Neşet Ertaş - Neredesin Sen",
        "Cengiz Özkan - Kırmızı Gül Demet Demet",
        "Ertaş Kardeşler - Yolcu"
    )

    private val newsSongs = listOf(
        "NTV - Dakika Başı Haber Bülteni",
        "TRT - Gün Ortası Ekonomi Raporu",
        "A Spor - Transfer Günlüğü Canlı",
        "NTV Spor - Son Dakika Gelişmeleri",
        "TRT - Hava ve Yol Durumu Analizi"
    )

    private val defaultSongs = listOf(
        "Canlı Yayın Akışı",
        "Müzik Şöleni Kesintisiz",
        "NHH Radio - Keyifli Dinlemeler"
    )

    fun getRandomSongForStation(station: RadioStation): String {
        val list = when (station.genre) {
            "Pop" -> popSongs
            "Slow" -> slowSongs
            "Arabesk" -> arabeskSongs
            "Halk Müziği" -> halkSongs
            "Haber/Spor" -> newsSongs
            else -> defaultSongs
        }
        return list.random()
    }
}
