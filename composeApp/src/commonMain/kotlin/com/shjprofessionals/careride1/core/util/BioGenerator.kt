package com.shjprofessionals.careride1.core.util

import com.shjprofessionals.careride1.domain.model.DoctorProfile
import com.shjprofessionals.careride1.domain.model.Specialty

/**
 * Generates smart default bios and content for doctors based on their specialty
 */
object BioGenerator {

    /**
     * Generate a default bio based on specialty and other profile info
     */
    fun generateBio(profile: DoctorProfile): String {
        val specialty = profile.specialty
        val years = profile.yearsOfExperience
        val name = profile.name.split(" ").lastOrNull() ?: "I"

        val intro = when {
            years == 0 -> "As a dedicated ${ specialty.displayName.lowercase()} physician, I am committed to providing exceptional care to all my patients."
            years < 5 -> "With $years years of experience in ${specialty.displayName.lowercase()}, I bring fresh perspectives combined with evidence-based practices to patient care."
            years < 10 -> "With over $years years of experience in ${specialty.displayName.lowercase()}, I have developed expertise in treating a wide range of conditions."
            years < 20 -> "With more than $years years of dedicated practice in ${specialty.displayName.lowercase()}, I have helped thousands of patients achieve better health outcomes."
            else -> "As a veteran ${specialty.displayName.lowercase()} physician with over $years years of experience, I have dedicated my career to advancing patient care and mentoring the next generation of doctors."
        }

        val specialtyFocus = getSpecialtyFocus(specialty)

        val closing = "I believe in a patient-centered approach, taking the time to listen to your concerns and working together to develop a personalized treatment plan. My goal is to help you achieve optimal health and well-being."

        return "$intro\n\n$specialtyFocus\n\n$closing"
    }

    private fun getSpecialtyFocus(specialty: Specialty): String = when (specialty) {
        Specialty.GENERAL_PRACTICE ->
            "My practice focuses on comprehensive primary care, including preventive medicine, chronic disease management, and acute care for patients of all ages. I emphasize building long-term relationships with my patients to provide continuity of care."

        Specialty.CARDIOLOGY ->
            "I specialize in the diagnosis and treatment of heart conditions, including coronary artery disease, heart failure, arrhythmias, and hypertension. I utilize the latest diagnostic technologies and treatment approaches to help my patients maintain heart health."

        Specialty.DERMATOLOGY ->
            "My expertise covers the full spectrum of skin conditions, from acne and eczema to skin cancer screening and cosmetic dermatology. I am committed to helping patients achieve healthy, beautiful skin through personalized treatment plans."

        Specialty.PEDIATRICS ->
            "I am passionate about providing compassionate, comprehensive care for children from newborns through adolescence. My focus includes well-child visits, vaccinations, developmental assessments, and treatment of childhood illnesses."

        Specialty.ORTHOPEDICS ->
            "I specialize in the diagnosis and treatment of musculoskeletal conditions, including sports injuries, joint problems, fractures, and spine disorders. I offer both surgical and non-surgical treatment options tailored to each patient's needs."

        Specialty.NEUROLOGY ->
            "My practice focuses on disorders of the nervous system, including headaches, epilepsy, stroke, multiple sclerosis, and movement disorders. I utilize advanced diagnostic techniques to provide accurate diagnoses and effective treatment plans."

        Specialty.PSYCHIATRY ->
            "I provide comprehensive mental health care, including treatment for depression, anxiety, bipolar disorder, ADHD, and other psychiatric conditions. I believe in a holistic approach that may include therapy, medication management, and lifestyle modifications."

        Specialty.GYNECOLOGY ->
            "I am dedicated to women's health across all life stages, providing care for reproductive health, pregnancy, menopause, and gynecological conditions. I create a comfortable, supportive environment for discussing sensitive health concerns."

        Specialty.OPHTHALMOLOGY ->
            "I specialize in comprehensive eye care, from routine vision exams to the diagnosis and treatment of eye diseases including cataracts, glaucoma, and macular degeneration. I utilize state-of-the-art technology to preserve and improve my patients' vision."

        Specialty.ENT ->
            "My practice encompasses disorders of the ear, nose, and throat, including hearing loss, sinusitis, allergies, and sleep disorders. I offer both medical and surgical treatments to help patients breathe, hear, and live better."
    }

    /**
     * Generate suggested areas of expertise based on specialty
     */
    fun suggestExpertise(specialty: Specialty): List<String> = when (specialty) {
        Specialty.GENERAL_PRACTICE -> listOf(
            "Preventive Care", "Chronic Disease Management", "Acute Care",
            "Health Screenings", "Immunizations", "Minor Procedures",
            "Weight Management", "Diabetes Care", "Hypertension Management"
        )
        Specialty.CARDIOLOGY -> listOf(
            "Heart Disease Prevention", "Coronary Artery Disease", "Heart Failure",
            "Arrhythmia Management", "Hypertension", "Cholesterol Management",
            "Echocardiography", "Stress Testing", "Cardiac Rehabilitation"
        )
        Specialty.DERMATOLOGY -> listOf(
            "Acne Treatment", "Eczema & Psoriasis", "Skin Cancer Screening",
            "Mole Evaluation", "Cosmetic Dermatology", "Botox & Fillers",
            "Laser Treatments", "Rosacea", "Hair Loss"
        )
        Specialty.PEDIATRICS -> listOf(
            "Well-Child Care", "Vaccinations", "Developmental Assessments",
            "ADHD", "Asthma Management", "Allergies", "Newborn Care",
            "Adolescent Medicine", "Behavioral Health"
        )
        Specialty.ORTHOPEDICS -> listOf(
            "Joint Replacement", "Sports Medicine", "Spine Surgery",
            "Fracture Care", "Arthroscopy", "Physical Therapy",
            "Regenerative Medicine", "Shoulder Injuries", "Knee Injuries"
        )
        Specialty.NEUROLOGY -> listOf(
            "Headache & Migraine", "Epilepsy", "Stroke Care",
            "Multiple Sclerosis", "Parkinson's Disease", "Dementia",
            "Neuropathy", "Sleep Disorders", "Concussion Management"
        )
        Specialty.PSYCHIATRY -> listOf(
            "Depression", "Anxiety Disorders", "Bipolar Disorder",
            "ADHD", "PTSD", "OCD", "Medication Management",
            "Psychotherapy", "Addiction Psychiatry"
        )
        Specialty.GYNECOLOGY -> listOf(
            "Annual Exams", "Contraception", "Menopause Management",
            "PCOS", "Endometriosis", "Fertility Counseling",
            "Minimally Invasive Surgery", "Prenatal Care", "HPV Screening"
        )
        Specialty.OPHTHALMOLOGY -> listOf(
            "Cataract Surgery", "Glaucoma Treatment", "LASIK",
            "Macular Degeneration", "Diabetic Eye Care", "Dry Eye",
            "Pediatric Eye Care", "Contact Lens Fitting", "Eye Infections"
        )
        Specialty.ENT -> listOf(
            "Sinus Treatment", "Hearing Loss", "Sleep Apnea",
            "Tonsil & Adenoid Surgery", "Voice Disorders", "Allergies",
            "Ear Infections", "Thyroid Surgery", "Head & Neck Cancer"
        )
    }

    /**
     * Generate suggested conditions treated based on specialty
     */
    fun suggestConditions(specialty: Specialty): List<String> = when (specialty) {
        Specialty.GENERAL_PRACTICE -> listOf(
            "Diabetes", "Hypertension", "High Cholesterol", "Asthma",
            "Allergies", "Infections", "Thyroid Disorders", "Arthritis",
            "Anxiety", "Depression", "Back Pain", "Obesity"
        )
        Specialty.CARDIOLOGY -> listOf(
            "Coronary Artery Disease", "Heart Failure", "Atrial Fibrillation",
            "Hypertension", "Heart Valve Disease", "Cardiomyopathy",
            "Peripheral Artery Disease", "Palpitations", "Chest Pain"
        )
        Specialty.DERMATOLOGY -> listOf(
            "Acne", "Eczema", "Psoriasis", "Rosacea", "Skin Cancer",
            "Warts", "Fungal Infections", "Hives", "Vitiligo",
            "Hair Loss", "Nail Disorders", "Hyperhidrosis"
        )
        Specialty.PEDIATRICS -> listOf(
            "Common Cold", "Ear Infections", "Asthma", "Allergies",
            "ADHD", "Autism Spectrum", "Growth Disorders", "Obesity",
            "Diabetes", "Eczema", "Behavioral Issues"
        )
        Specialty.ORTHOPEDICS -> listOf(
            "Arthritis", "Back Pain", "Herniated Disc", "Carpal Tunnel",
            "Rotator Cuff Injury", "ACL Tear", "Meniscus Tear",
            "Fractures", "Osteoporosis", "Tendinitis", "Bursitis"
        )
        Specialty.NEUROLOGY -> listOf(
            "Migraine", "Tension Headache", "Epilepsy", "Stroke",
            "Multiple Sclerosis", "Parkinson's Disease", "Alzheimer's",
            "Neuropathy", "Vertigo", "Tremor", "Bell's Palsy"
        )
        Specialty.PSYCHIATRY -> listOf(
            "Major Depression", "Generalized Anxiety", "Panic Disorder",
            "Bipolar Disorder", "Schizophrenia", "PTSD", "OCD",
            "ADHD", "Eating Disorders", "Insomnia", "Substance Use"
        )
        Specialty.GYNECOLOGY -> listOf(
            "PCOS", "Endometriosis", "Uterine Fibroids", "Ovarian Cysts",
            "Abnormal Bleeding", "Pelvic Pain", "Menopause Symptoms",
            "Infertility", "Vaginal Infections", "Urinary Incontinence"
        )
        Specialty.OPHTHALMOLOGY -> listOf(
            "Cataracts", "Glaucoma", "Macular Degeneration", "Diabetic Retinopathy",
            "Dry Eye", "Conjunctivitis", "Keratoconus", "Retinal Detachment",
            "Floaters", "Strabismus", "Amblyopia"
        )
        Specialty.ENT -> listOf(
            "Sinusitis", "Tonsillitis", "Ear Infections", "Hearing Loss",
            "Tinnitus", "Sleep Apnea", "Allergic Rhinitis", "Deviated Septum",
            "Voice Disorders", "Thyroid Nodules", "Vertigo"
        )
    }

    /**
     * Generate a treatment philosophy based on specialty
     */
    fun generatePhilosophy(specialty: Specialty): String = when (specialty) {
        Specialty.GENERAL_PRACTICE ->
            "I believe in treating the whole person, not just symptoms. By building lasting relationships with my patients, I can provide personalized care that addresses both immediate concerns and long-term health goals."

        Specialty.CARDIOLOGY ->
            "Prevention is the cornerstone of my practice. I work closely with patients to identify risk factors early and develop comprehensive plans that combine lifestyle modifications with the latest medical treatments."

        Specialty.DERMATOLOGY ->
            "Healthy skin is essential to overall well-being and confidence. I combine medical expertise with an aesthetic eye to help patients achieve results that make them feel comfortable in their own skin."

        Specialty.PEDIATRICS ->
            "Every child deserves a doctor who listens. I partner with parents to support each child's unique developmental journey while making medical visits a positive experience for the whole family."

        Specialty.ORTHOPEDICS ->
            "My goal is to get you back to doing what you love. Whether through conservative treatments or surgery, I focus on restoring function and quality of life with minimal downtime."

        Specialty.NEUROLOGY ->
            "Neurological conditions can be complex and life-changing. I take time to explain diagnoses clearly and work collaboratively with patients to find treatments that improve their daily lives."

        Specialty.PSYCHIATRY ->
            "Mental health is just as important as physical health. I provide a safe, non-judgmental space where we can work together to understand your challenges and develop a path toward wellness."

        Specialty.GYNECOLOGY ->
            "I believe every woman deserves compassionate, comprehensive care in a comfortable environment. I'm here to support you through every stage of life with respect and understanding."

        Specialty.OPHTHALMOLOGY ->
            "Your vision is precious. I use the most advanced techniques to preserve and enhance your sight, always taking time to explain your options and answer your questions."

        Specialty.ENT ->
            "Breathing, hearing, and speaking are fundamental to quality of life. I take a thorough approach to diagnosis and offer personalized solutions that address the root cause of your symptoms."
    }

    /**
     * Common insurance providers for suggestions
     */
    val commonInsuranceProviders = listOf(
        "Aetna",
        "Anthem Blue Cross",
        "Blue Cross Blue Shield",
        "Cigna",
        "Humana",
        "Kaiser Permanente",
        "Medicare",
        "Medicaid",
        "Oscar Health",
        "UnitedHealthcare",
        "Self-Pay"
    )

    /**
     * Common languages for suggestions
     */
    val commonLanguages = listOf(
        "English",
        "Spanish",
        "Mandarin",
        "Cantonese",
        "Tagalog",
        "Vietnamese",
        "Korean",
        "Russian",
        "Arabic",
        "Hindi",
        "Portuguese",
        "French",
        "German",
        "Japanese",
        "Italian",
        "Polish",
        "American Sign Language (ASL)"
    )

    /**
     * US States for dropdown
     */
    val usStates = listOf(
        "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
        "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
        "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
        "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
        "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY", "DC"
    )
}