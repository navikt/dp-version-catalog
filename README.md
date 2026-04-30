# dp-version-catalog

Inneholder sentrale 3. parts biblioteker som brukes i Dagpenger applikasjoner i form av [gradle version catalog](https://docs.gradle.org/current/userguide/platforms.html).

## Oppdatering av versjoner
 
Automatisk 

- Dependabot brukes for å oppdage nye versjoner av biblioteker. (merge dependabot PRs)

Manuelt
- Oppdater versjon i `gradle/libs.version.toml` og commit endringen.


Ny versjon [releases](https://github.com/navikt/dp-version-catalog/releases)

## Migrering til Jackson 3.x

Fra og med katalogversjon `20260430.*` er Jackson oppgradert til **3.x** (`tools.jackson.*`).
Dette er en breaking change. Se [Jackson 3 release notes](https://github.com/FasterXML/jackson/wiki/Jackson-Release-3.0#behavioral-changes).

### Importer

```kotlin
// Før (Jackson 2.x)
import com.fasterxml.jackson.databind.ObjectMapper

// Etter (Jackson 3.x)
import tools.jackson.databind.ObjectMapper
```

### Ktor ContentNegotiation

Funksjonsnavnet er uendret — kun pakken endres:

```kotlin
// Før
import io.ktor.serialization.jackson.jackson

// Etter
import io.ktor.serialization.jackson3.jackson  // ← ny pakke, samme funksjonsnavn

install(ContentNegotiation) { jackson { } }  // uendret
```

### ⚠️ Kritisk: Norske tegn i Kotlin-properties

Jackson 3 er strengere på getter-navngiving. Kotlin-properties med ikke-ASCII første tegn (f.eks. `ønsker`, `årsak`, `ærlig`) genererer Java-gettere som Jackson 3 **ikke** plukker opp som standard — de **droppes lydløst fra JSON**.

**Eksempel på problemet:**
```kotlin
data class Søknad(val ønsker: String) // genererer getønsker() i Java
```
Jackson 3 ignorerer `getønsker()` fordi `ø` ikke er ASCII uppercase etter `get`.

**Løsning — konfigurer ObjectMapper eksplisitt:**
```kotlin
private val objectMapper = jacksonMapperBuilder()
    .accessorNaming(
        DefaultAccessorNamingStrategy.Provider()
            .withFirstCharAcceptance(true, true)
        // true #1: getter med lowercase/non-ASCII første tegn inkluderes
        // true #2: felter som starter med _ eller $ inkluderes
    )
    .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .build()
```

Se også: [jackson-module-kotlin#1154](https://github.com/FasterXML/jackson-module-kotlin/issues/1154)

### Endrede standardverdier i Jackson 3

To innstillinger som tidligere måtte skrus **av** eksplisitt er nå **av som standard**:

| Innstilling | Jackson 2 | Jackson 3 |
|-------------|-----------|-----------|
| `WRITE_DATES_AS_TIMESTAMPS` | ✅ på | ❌ av |
| `FAIL_ON_UNKNOWN_PROPERTIES` | ✅ på | ❌ av |

Det betyr:
- `.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)` — **kan fjernes**, men er ufarlig å beholde
- `.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)` — **kan fjernes**, men er ufarlig å beholde
- Kode som **ikke** eksplisitt skrur på disse vil nå oppføre seg annerledes enn i Jackson 2

### JavaTimeModule

`jackson-datatype-jsr310` er ikke lenger nødvendig som separat avhengighet — Java Time-støtte er innebygget i `jackson-databind` 3.x.
Fjern manuell registrering av `JavaTimeModule` hvis dere har det.

### bundles.jackson

```kotlin
// Før
implementation(libs.bundles.jackson) // jackson-core, jackson-annotation, jackson-datatype-jsr310, jackson-kotlin

// Etter
implementation(libs.bundles.jackson) // jackson-databind, jackson-kotlin
```

