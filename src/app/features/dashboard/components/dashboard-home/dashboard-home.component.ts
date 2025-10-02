import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

import { TaskService } from '../../../tasks/services/task.service';
import { AuthService } from '../../../../core/services/auth.service';
import { Task, TaskStatus, TaskPriority } from '../../../tasks/models/task.model';
import { TaskFormComponent } from '../../../tasks/components/task-form/task-form.component';
import { NotificationService } from '../../../../core/services/notification.service';

interface DashboardStats {
  totalTasks: number;
  completedTasks: number;
  pendingTasks: number;
  highPriorityTasks: number;
}

@Component({
  selector: 'app-dashboard-home',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatDialogModule
  ],
  templateUrl: './dashboard-home.component.html',
  styleUrls: ['./dashboard-home.component.scss']
})
export class DashboardHomeComponent implements OnInit {
  currentUser: any;
  stats: DashboardStats = {
    totalTasks: 0,
    completedTasks: 0,
    pendingTasks: 0,
    highPriorityTasks: 0
  };
  recentTasks: Task[] = [];
  loading = true;

  // Expose enums to template
  TaskStatus = TaskStatus;
  TaskPriority = TaskPriority;

  constructor(
    private taskService: TaskService,
    private authService: AuthService,
    private router: Router,
    private dialog: MatDialog,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadCurrentUser();
    this.loadDashboardData();
  }

  /**
   * Load current user information
   */
  private loadCurrentUser(): void {
    this.authService.currentUser$.subscribe({
      next: (user) => {
        this.currentUser = user;
      },
      error: (error) => {
        console.error('Error loading user:', error);
      }
    });
  }

  /**
   * Load all dashboard data including stats and recent tasks
   */
  loadDashboardData(): void {
    this.loading = true;

    this.taskService.getAllTasks().subscribe({
      next: (tasks) => {
        this.calculateStats(tasks);
        this.recentTasks = this.getRecentTasks(tasks, 5);
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading dashboard data:', error);
        this.loading = false;
        this.notificationService.showError('Error loading dashboard data');
      }
    });
  }

  /**
   * Calculate dashboard statistics from tasks
   */
  private calculateStats(tasks: Task[]): void {
    this.stats.totalTasks = tasks.length;

    this.stats.completedTasks = tasks.filter(
      task => task.status === TaskStatus.COMPLETED
    ).length;

    this.stats.pendingTasks = tasks.filter(
      task => task.status !== TaskStatus.COMPLETED
    ).length;

    this.stats.highPriorityTasks = tasks.filter(
      task => task.priority === TaskPriority.HIGH
    ).length;
  }

  /**
   * Get most recent tasks sorted by creation date
   */
  private getRecentTasks(tasks: Task[], limit: number): Task[] {
    return tasks
      .sort((a, b) => {
        const dateA = new Date(a.createdAt || 0).getTime();
        const dateB = new Date(b.createdAt || 0).getTime();
        return dateB - dateA;
      })
      .slice(0, limit);
  }

  /**
   * Open dialog to create a new task
   */
  openNewTaskDialog(): void {
    const dialogRef = this.dialog.open(TaskFormComponent, {
      width: '600px',
      maxWidth: '90vw',
      disableClose: false,
      data: null // Pass null for create mode
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        // Task was created successfully
        this.loadDashboardData();
      }
    });
  }

  /**
   * Navigate to tasks page
   */
  navigateToTasks(): void {
    this.router.navigate(['/tasks']);
  }

  /**
   * View/Edit task - opens edit dialog
   */
  viewTask(task: Task): void {
    const dialogRef = this.dialog.open(TaskFormComponent, {
      width: '600px',
      maxWidth: '90vw',
      disableClose: false,
      data: task // Pass the task for edit mode
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        // Task was updated successfully
        this.loadDashboardData();
      }
    });
  }

  /**
   * Toggle task completion status
   */
  toggleTaskComplete(task: Task, event?: Event): void {
    if (event) {
      event.stopPropagation();
    }

    const newStatus = task.status === TaskStatus.COMPLETED
      ? TaskStatus.TODO
      : TaskStatus.COMPLETED;

    this.taskService.updateTaskStatus(task.id!, newStatus).subscribe({
      next: () => {
        this.notificationService.showSuccess(
          `Task marked as ${newStatus === TaskStatus.COMPLETED ? 'completed' : 'incomplete'}`
        );
        this.loadDashboardData();
      },
      error: (error) => {
        console.error('Error updating task status:', error);
        this.notificationService.showError('Error updating task status');
      }
    });
  }

  /**
   * Get priority color class
   */
  getPriorityClass(priority: TaskPriority): string {
    return `priority-${priority.toLowerCase()}`;
  }

  /**
   * Get status color class
   */
  getStatusClass(status: TaskStatus): string {
    return `status-${status.toLowerCase().replace('_', '-')}`;
  }

  /**
   * Check if task is overdue
   */
  isTaskOverdue(task: Task): boolean {
    if (!task.dueDate) return false;

    const dueDate = new Date(task.dueDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    return dueDate < today && task.status !== TaskStatus.COMPLETED;
  }

  /**
   * Format task date
   */
  formatDate(date: Date | string): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });
  }

  /**
   * Refresh dashboard data
   */
  refreshDashboard(): void {
    this.loadDashboardData();
  }
}
