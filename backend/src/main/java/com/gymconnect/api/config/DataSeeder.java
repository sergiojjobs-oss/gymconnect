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
            "Entrenador personal y nutricionista deportivo certificado con más de 5 años de experiencia. Me especializo en hipertrofia muscular con planes nutricionales integrados para conseguir resultados duraderos en sala de musculación.",
            39.0, 4.9, 128, true, 5,
            List.of("Musculación/Hipertrofia", "Nutrición deportiva", "Pérdida de grasa"),
            List.of("Plan de entrenamiento semanal personalizado","Dieta y seguimiento de macros","Chat directo ilimitado","Revisión mensual de progreso","Ajustes del plan cada 4 semanas","Material de vídeo explicativo"),
            "linear-gradient(135deg,#16a34a,#84cc16)", Usuario.PlanSuscripcion.PRO);

        crearEntrenador("Ana",     "Martínez",  "ana.martinez@gymconnect.es",      "Barcelona",
            "Entrenadora personal especializada en entrenamiento femenino y definición. Ayudo a mujeres a transformar su cuerpo en el gimnasio con rutinas de tonificación, peso libre y planificación nutricional.",
            29.0, 4.9, 94, true, 4,
            List.of("Entrenamiento femenino", "Definición y tonificación", "Pérdida de grasa", "Nutrición deportiva"),
            List.of("Rutina de tonificación personalizada","Plan nutricional equilibrado","Seguimiento semanal por chat","Ejercicios con peso libre y máquinas","Recetas y control de macros"),
            "linear-gradient(135deg,#059669,#34d399)", Usuario.PlanSuscripcion.PRO);

        crearEntrenador("Jorge",   "López",     "jorge.lopez@gymconnect.es",       "Sevilla",
            "Preparador físico especializado en fuerza máxima y powerlifting con 7 años de experiencia. He preparado atletas para competiciones regionales de powerlifting y levantamiento de pesas.",
            49.0, 4.7, 61, false, 7,
            List.of("Fuerza y potencia", "Powerlifting", "Musculación/Hipertrofia"),
            List.of("Programación de fuerza periodizada","Análisis de técnica en vídeo","Chat directo con respuesta en 24h","Seguimiento de marcas y récords","Planificación de competición"),
            "linear-gradient(135deg,#d97706,#f59e0b)", Usuario.PlanSuscripcion.FREE);

        crearEntrenador("Laura",   "Sánchez",   "laura.sanchez@gymconnect.es",     "Valencia",
            "Nutricionista deportiva y entrenadora personal con doble titulación. Me especializo en diseñar planes de alimentación personalizados integrados con rutinas de musculación y ganancia de masa muscular.",
            55.0, 5.0, 47, true, 6,
            List.of("Nutrición deportiva", "Pérdida de grasa", "Ganancia de masa muscular"),
            List.of("Análisis metabólico inicial","Plan nutricional con seguimiento de macros","Entrenamiento de musculación complementario","Control de composición corporal","Soporte por chat ilimitado"),
            "linear-gradient(135deg,#7c3aed,#a78bfa)", Usuario.PlanSuscripcion.ELITE);

        crearEntrenador("Marcos",  "Fernández", "marcos.fernandez@gymconnect.es",  "Bilbao",
            "Especialista en entrenamiento con peso libre y barras. Metodología de progresión lineal y ondulada para maximizar la fuerza en sentadilla, peso muerto y press banca.",
            35.0, 4.6, 39, false, 3,
            List.of("Peso libre y barras", "Fuerza y potencia", "Musculación/Hipertrofia"),
            List.of("Programa de fuerza periodizado","Técnica de levantamientos básicos","Chat de seguimiento semanal","Revisión de vídeos de entrenamiento"),
            "linear-gradient(135deg,#0ea5e9,#38bdf8)", Usuario.PlanSuscripcion.FREE);

        crearEntrenador("Sofía",   "Morales",   "sofia.morales@gymconnect.es",     "Málaga",
            "Entrenadora personal especializada en movilidad, core y entrenamiento femenino. Trabajo la flexibilidad y el trabajo de abdomen para mejorar el rendimiento en sala y prevenir lesiones.",
            25.0, 4.8, 83, false, 5,
            List.of("Core y abdomen", "Flexibilidad y movilidad", "Entrenamiento femenino"),
            List.of("Rutinas de core y abdomen funcional","Mejora de movilidad articular","Plan de entrenamiento femenino","Prevención de lesiones","Seguimiento por chat"),
            "linear-gradient(135deg,#ec4899,#f9a8d4)", Usuario.PlanSuscripcion.FREE);

        crearEntrenador("Rubén",   "Torres",    "ruben.torres@gymconnect.es",      "Madrid",
            "Entrenador de sala con 8 años de experiencia en HIIT, circuitos de alta intensidad y cardio de gimnasio. Especializado en quema de grasa y mejora de la condición física en sala.",
            60.0, 4.5, 55, true, 8,
            List.of("HIIT en sala", "Cardio de gimnasio", "Pérdida de grasa"),
            List.of("Circuitos HIIT personalizados","Programación de cardio en sala","Análisis de composición corporal","Seguimiento de rendimiento","Nutrición para pérdida de grasa"),
            "linear-gradient(135deg,#16a34a,#065f46)", Usuario.PlanSuscripcion.PRO);

        crearEntrenador("Elena",   "Castro",    "elena.castro@gymconnect.es",      "Zaragoza",
            "Bioquímica y nutricionista deportiva con 6 años de experiencia. Diseño planes de alimentación basados en tu perfil metabólico para optimizar la ganancia muscular y pérdida de grasa.",
            32.0, 4.9, 71, true, 6,
            List.of("Nutrición deportiva", "Pérdida de grasa", "Ganancia de masa muscular"),
            List.of("Análisis metabólico personalizado","Plan nutricional basado en evidencia","Entrenamiento de musculación","Suplementación deportiva","Seguimiento mensual detallado"),
            "linear-gradient(135deg,#f59e0b,#fde68a)", Usuario.PlanSuscripcion.PRO);

        crearEntrenador("Iván",    "Romero",    "ivan.romero@gymconnect.es",       "Alicante",
            "Entrenador personal de gimnasio con tarifas accesibles. Ideal para principiantes que quieren iniciarse en el mundo de la musculación y el entrenamiento en sala de forma segura y efectiva.",
            20.0, 4.3, 28, false, 2,
            List.of("Entrenamiento online", "Musculación/Hipertrofia", "Entrenamiento en máquinas"),
            List.of("Plan de iniciación al gimnasio","Guía de uso de máquinas","Chat de soporte","Ajustes mensuales del plan"),
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
