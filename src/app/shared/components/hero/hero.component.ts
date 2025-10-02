import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-hero',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './hero.component.html',
  styleUrls: ['./hero.component.scss']
})
export class HeroComponent {
  constructor(private router: Router) {}

  onGetStarted(): void {
    this.router.navigate(['/auth/register']);
  }

  onDiscoverVideo(): void {
    // Open video modal or navigate to video page
    console.log('Discover video clicked');
  }
}
