
document.addEventListener('DOMContentLoaded', () => {
    console.log("GameHarbor Frontend Initialized successfully.");
    
    // Interactividad global para botones y componentes visuales
    const buttons = document.querySelectorAll('.btn-steam');
    buttons.forEach(btn => {
        btn.addEventListener('click', () => {
            console.log("Acción ejecutada en el componente visual.");
        });
    });

    // Función base para futuras peticiones Fetch hacia el backend Java (Spring Boot)
    window.fetchGameData = async function(endpoint) {
        try {
            const response = await fetch(endpoint);
            if (!response.ok) throw new Error('Error al conectar con el servidor Java');
            return await response.json();
        } catch (error) {
            console.error("Fetch error:", error);
            return null;
        }
    };
});