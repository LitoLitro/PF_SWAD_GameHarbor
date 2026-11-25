export interface Categoria { id: number; nombre: string; descripcion: string; }
export interface MetodoPago { id: number; nombre: string; }
export interface JuegoResumen { id: number; titulo: string; precio: number; imagenUrl: string | null; requiereCloud: boolean; desarrollador: string; categorias: string[]; }
export interface JuegoDetalle extends JuegoResumen { descripcion: string; fechaLanzamiento: string; estado: string; calificacionPromedio: number | null; totalResenas: number; }
export interface Pagina<T> { contenido: T[]; pagina: number; tamano: number; totalElementos: number; totalPaginas: number; }
export interface Resena { id: number; usuario: string; calificacion: number; comentario: string; fecha: string; }
export interface CarritoItem { juegoId: number; titulo: string; imagenUrl: string | null; precio: number; cantidad: number; }
export interface Carrito { items: CarritoItem[]; total: number; }
export interface OrdenDetalle { juegoId: number; titulo: string; precioUnitario: number; cantidad: number; }
export interface Orden { id: number; fecha: string; total: number; estado: string; metodoPago: string; detalles: OrdenDetalle[]; }
export interface BibliotecaItem { juegoId: number; titulo: string; imagenUrl: string | null; requiereCloud: boolean; fechaAdquisicion: string; }
export interface SesionCloud { id: number; juegoId: number; juegoTitulo: string; servidor: string; ubicacion: string; estado: string; fechaInicio: string; fechaFin: string | null; bitrateMbps: number | null; latenciaMs: number | null; }
export interface Recomendacion { juego: JuegoResumen; puntaje: number; origen: string; }
export interface HistorialRequest { juegoId: number; segundos: number; origen: 'CATALOGO' | 'BUSQUEDA' | 'BIBLIOTECA' | 'RECOMENDACION'; }
