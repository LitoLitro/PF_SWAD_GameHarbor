import { Component, input } from '@angular/core';

@Component({ selector: 'app-empty-state', template: '<section class="state"><span class="state-icon" aria-hidden="true">◇</span><h3>{{ title() }}</h3><p>{{ message() }}</p></section>', styles: [`.state{display:grid;justify-items:center;padding:2.5rem 1.25rem;border:1px dashed #486078;border-radius:.7rem;background:rgb(33 50 71 / 55%);text-align:center}.state-icon{color:var(--steam-blue);font-size:2rem}.state h3{margin:.6rem 0;color:var(--text-white)}.state p{max-width:34rem;margin:0;color:var(--text-muted);line-height:1.55}`] })
export class EmptyStateComponent { readonly title = input.required<string>(); readonly message = input.required<string>(); }
