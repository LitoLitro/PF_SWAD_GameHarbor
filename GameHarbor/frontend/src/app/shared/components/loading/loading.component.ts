import { Component, input } from '@angular/core';

@Component({ selector: 'app-loading', template: '<div class="loading" role="status"><span class="spinner" aria-hidden="true"></span><span>{{ label() }}</span></div>', styles: [`.loading{display:flex;align-items:center;gap:.75rem;color:var(--text-main)}.spinner{width:1.1rem;height:1.1rem;border:2px solid #49627a;border-top-color:var(--steam-blue);border-radius:50%;animation:spin .8s linear infinite}@keyframes spin{to{transform:rotate(360deg)}}@media(prefers-reduced-motion:reduce){.spinner{animation:none}}`] })
export class LoadingComponent { readonly label = input('Cargando…'); }
