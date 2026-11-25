import { Component, input } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { GameCardData } from '../../models/game-card.model';

@Component({
  selector: 'app-game-card',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './game-card.component.html',
  styleUrl: './game-card.component.css',
})
export class GameCardComponent {
  readonly game = input.required<GameCardData>();
}

