package com.gymconnect.api.config;

import com.gymconnect.api.model.Entrenador;
import com.gymconnect.api.model.Usuario;
import com.gymconnect.api.repository.EntrenadorRepository;
import com.gymconnect.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UsuarioRepository usuarioRepo;
    private final EntrenadorRepository entrenadorRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (entrenadorRepo.count() > 0) return;

        crearEntrenador("Carlos",  "Ruiz",      "carlos.ruiz@gymconnect.es",      "Madrid",
            "Soy entrenador personal y nutricionista deportivo certificado con más de 5 años de experiencia. Me especializo en combinar entrenamiento de hipertrofia con planes nutricionales integrales para conseguir resultados duraderos.",
            39.0, 4.9, 128, true, 5,
            List.of("musculacion", "nutricion", "perdida"),
            List.of("Plan de entrenamiento semanal personalizado","Dieta y seguimiento de macros","Chat directo ilimitado","Revisión mensual de progreso","Ajustes del plan cada 4 semanas","Material de vídeo explicativo"),
            "linear-gradient(135deg,#16a34a,#84cc16)", Usuario.PlanSuscripcion.PRO);

        crearEntrenador("Ana",     "Martínez",  "ana.martinez@gymconnect.es",      "Barcelona",
            "Soy dietista y coach de bienestar. Mi metodología combina yoga, pilates y nutrición equilibrada para una transformación física y mental completa.",
            29.0, 4.9, 94, true, 4,
            List.of("yoga", "perdida", "nutricion"),
            List.of("Rutina de yoga y pilates personalizada","Plan nutricional equilibrado","Seguimiento semanal por chat","Meditación y gestión del estrés","Recetas saludables semanales"),
            "linear-gradient(135deg,#059669,#34d399)", Usuario.PlanSuscripcion.PRO);

        crearEntrenador("Jorge",   "López",     "jorge.lopez@gymconnect.es",       "Sevilla",
            "Preparador físico especializado en fuerza funcional y Crossfit con 7 años de experiencia. He preparado atletas para competiciones regionales y nacionales.",
            49.0, 4.7, 61, false, 7,
            List.of("fuerza", "crossfit", "musculacion"),
            List.of("Programación de fuerza y WODs","Análisis de técnica en vídeo","Chat directo con respuesta en 24h","Seguimiento de marcas y récords","Planificación de competición"),
            "linear-gradient(135deg,#d97706,#f59e0b)", Usuario.PlanSuscripcion.FREE);

        crearEntrenador("Laura",   "Sánchez",   "laura.sanchez@gymconnect.es",     "Valencia",
            "Nutricionista deportiva y entrenadora personal con doble titulación. Me especializo en diseñar planes de alimentación personalizados integrados con el entrenamiento.",
            55.0, 5.0, 47, true, 6,
            List.of("nutricion", "perdida", "musculacion"),
            List.of("Análisis metabólico inicial","Plan nutricional con seguimiento de macros","Entrenamiento complementario","Control de composición corporal","Soporte por chat ilimitado"),
            "linear-gradient(135deg,#7c3aed,#a78bfa)", Usuario.PlanSuscripcion.ELITE);

        crearEntrenador("Marcos",  "Fernández", "marcos.fernandez@gymconnect.es",  "Bilbao",
            "Preparador físico especializado en fuerza máxima y powerlifting. Metodología de progresión lineal y ondulada adaptada a cada atleta.",
            35.0, 4.6, 39, false, 3,
            List.of("fuerza", "musculacion"),
            List.of("Programa de fuerza periodizado","Técnica de levantamientos básicos","Chat de seguimiento semanal","Revisión de vídeos de entrenamiento"),
            "linear-gradient(135deg,#0ea5e9,#38bdf8)", Usuario.PlanSuscripcion.FREE);

        crearEntrenador("Sofía",   "Morales",   "sofia.morales@gymconnect.es",     "Málaga",
            "Instructora de yoga certificada y coach de hábitos saludables. Mi enfoque holístico combina movimiento consciente, nutrición intuitiva y técnicas de mindfulness.",
            25.0, 4.8, 83, false, 5,
            List.of("yoga", "perdida"),
            List.of("Clases de yoga personalizadas","Plan de hábitos saludables","Técnicas de mindfulness y respiración","Nutrición intuitiva","Comunidad de apoyo online"),
            "linear-gradient(135deg,#ec4899,#f9a8d4)", Usuario.PlanSuscripcion.FREE);

        crearEntrenador("Rubén",   "Torres",    "ruben.torres@gymconnect.es",      "Madrid",
            "Coach de Crossfit con certificación L2 y 8 años de experiencia. He entrenado a atletas de nivel regional y nacional.",
            60.0, 4.5, 55, true, 8,
            List.of("crossfit", "fuerza", "musculacion"),
            List.of("Programación Crossfit L2","Análisis de movimiento","Preparación para competición","Seguimiento de rendimiento","Nutrición para el rendimiento"),
            "linear-gradient(135deg,#16a34a,#065f46)", Usuario.PlanSuscripcion.PRO);

        crearEntrenador("Elena",   "Castro",    "elena.castro@gymconnect.es",      "Zaragoza",
            "Bioquímica y nutricionista deportiva con 6 años de experiencia. Diseño planes de alimentación basados en tu perfil metabólico.",
            32.0, 4.9, 71, true, 6,
            List.of("nutricion", "perdida", "musculacion"),
            List.of("Análisis metabólico y genético","Plan nutricional personalizado","Entrenamiento de recomposición","Suplementación basada en evidencia","Seguimiento mensual detallado"),
            "linear-gradient(135deg,#f59e0b,#fde68a)", Usuario.PlanSuscripcion.PRO);

        crearEntrenador("Iván",    "Romero",    "ivan.romero@gymconnect.es",       "Alicante",
            "Entrenador personal online con tarifas accesibles para que el fitness sea para todos. Especializado en principiantes.",
            20.0, 4.3, 28, false, 2,
            List.of("perdida", "musculacion"),
            List.of("Plan de entrenamiento para principiantes","Guía nutricional básica","Chat de soporte","Ajustes mensuales del plan"),
            "linear-gradient(135deg,#06b6d4,#67e8f9)", Usuario.PlanSuscripcion.FREE);
    }

    private void crearEntrenador(String nombre, String apellido, String email, String ciudad,
                                  String bio, Double precio, Double rating, int resenas,
                                  boolean verificado, int anios,
                                  List<String> especialidades, List<String> servicios,
                                  String avatarColor, Usuario.PlanSuscripcion plan) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode("Gymconnect2026!"));
        u.setRol(Usuario.Rol.ENTRENADOR);
        u.setPlan(plan);
        u = usuarioRepo.save(u);

        Entrenador e = new Entrenador();
        e.setUsuario(u);
        e.setCiudad(ciudad);
        e.setBio(bio);
        e.setPrecioMensual(precio);
        e.setRating(rating);
        e.setTotalResenas(resenas);
        e.setVerificado(verificado);
        e.setAniosExperiencia(anios);
        e.setEspecialidades(especialidades);
        e.setServicios(servicios);
        e.setAvatarColor(avatarColor);
        entrenadorRepo.save(e);
    }
}
