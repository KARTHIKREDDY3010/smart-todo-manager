(() => {
    'use strict';

    const API = '/api/tasks';
    const $ = (id) => document.getElementById(id);

    const taskModal = new bootstrap.Modal($('taskModal'));
    const deleteModal = new bootstrap.Modal($('deleteModal'));

    let tasks = [];
    let deleteId = null;
    let searchTimer = null;

    const STATUS_LABELS = { TODO: 'To Do', ONGOING: 'Ongoing', COMPLETED: 'Completed' };
    const PRIORITY_BADGES = {
        HIGH: 'bg-danger',
        MEDIUM: 'bg-warning text-dark',
        LOW: 'bg-secondary'
    };

    // ---------- helpers ----------

    function escapeHtml(value) {
        return String(value ?? '').replace(/[&<>"']/g, (c) => ({
            '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
        }[c]));
    }

    function todayString() {
        const d = new Date();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${d.getFullYear()}-${month}-${day}`;
    }

    function formatDate(dateStr) {
        if (!dateStr) return '-';
        const [y, m, d] = dateStr.split('-').map(Number);
        return new Date(y, m - 1, d).toLocaleDateString('en-IN', {
            day: '2-digit', month: 'short', year: 'numeric'
        });
    }

    function showToast(message, type = 'success') {
        const el = document.createElement('div');
        el.className = `toast align-items-center text-bg-${type} border-0`;
        el.setAttribute('role', 'alert');
        el.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">${escapeHtml(message)}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>`;
        $('toastContainer').appendChild(el);
        const toast = new bootstrap.Toast(el, { delay: 3000 });
        el.addEventListener('hidden.bs.toast', () => el.remove());
        toast.show();
    }

    async function request(url, options = {}) {
        const res = await fetch(url, {
            headers: { 'Content-Type': 'application/json' },
            ...options
        });
        if (res.status === 204) return null;
        const data = await res.json().catch(() => null);
        if (!res.ok) {
            const err = new Error((data && data.message) || 'Request failed');
            err.fieldErrors = data && data.fieldErrors;
            err.status = res.status;
            throw err;
        }
        return data;
    }

    // ---------- loading data ----------

    async function loadStats() {
        const s = await request(`${API}/stats`);
        $('statTotal').textContent = s.total;
        $('statPending').textContent = s.pending;
        $('statOngoing').textContent = s.ongoing;
        $('statCompleted').textContent = s.completed;
    }

    async function loadTasks() {
        const params = new URLSearchParams();
        const keyword = $('searchInput').value.trim();
        if (keyword) params.set('keyword', keyword);
        if ($('filterStatus').value) params.set('status', $('filterStatus').value);
        if ($('filterPriority').value) params.set('priority', $('filterPriority').value);

        const query = params.toString();
        tasks = await request(query ? `${API}?${query}` : API);
        renderTasks();
    }

    async function refresh() {
        try {
            await Promise.all([loadTasks(), loadStats()]);
        } catch (err) {
            showToast(`Could not load data: ${err.message}`, 'danger');
        }
    }

    // ---------- rendering ----------

    function renderTasks() {
        const body = $('taskTableBody');
        const today = todayString();

        $('emptyState').classList.toggle('d-none', tasks.length > 0);

        body.innerHTML = tasks.map((t) => {
            const overdue = t.dueDate && t.dueDate < today && t.status !== 'COMPLETED';
            const options = Object.keys(STATUS_LABELS).map((s) =>
                `<option value="${s}" ${t.status === s ? 'selected' : ''}>${STATUS_LABELS[s]}</option>`
            ).join('');

            return `
                <tr>
                    <td>
                        <div class="fw-semibold">${escapeHtml(t.title)}</div>
                        ${t.description ? `<div class="small text-muted task-description">${escapeHtml(t.description)}</div>` : ''}
                    </td>
                    <td><span class="badge ${PRIORITY_BADGES[t.priority] || 'bg-secondary'}">${escapeHtml(t.priority)}</span></td>
                    <td>
                        ${escapeHtml(formatDate(t.dueDate))}
                        ${overdue ? '<span class="badge bg-danger ms-1">Overdue</span>' : ''}
                    </td>
                    <td>
                        <select class="form-select form-select-sm status-select" data-id="${t.id}">${options}</select>
                    </td>
                    <td class="text-end text-nowrap">
                        <button class="btn btn-sm btn-outline-primary" data-action="edit" data-id="${t.id}">Edit</button>
                        <button class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${t.id}">Delete</button>
                    </td>
                </tr>`;
        }).join('');
    }

    // ---------- add / edit form ----------

    function clearFormErrors() {
        $('formError').classList.add('d-none');
        $('formError').textContent = '';
        ['taskTitle', 'taskDescription'].forEach((id) => {
            $(id).classList.remove('is-invalid');
            $(`${id}Error`).textContent = '';
        });
    }

    function openForm(task) {
        clearFormErrors();
        $('taskModalTitle').textContent = task ? 'Edit Task' : 'Add Task';
        $('taskId').value = task ? task.id : '';
        $('taskTitle').value = task ? task.title : '';
        $('taskDescription').value = task && task.description ? task.description : '';
        $('taskPriority').value = task ? task.priority : 'MEDIUM';
        $('taskStatus').value = task ? task.status : 'TODO';
        $('taskDueDate').value = task && task.dueDate ? task.dueDate : '';
        taskModal.show();
    }

    async function saveTask(event) {
        event.preventDefault();
        clearFormErrors();

        const id = $('taskId').value;
        const payload = {
            title: $('taskTitle').value.trim(),
            description: $('taskDescription').value.trim() || null,
            priority: $('taskPriority').value,
            status: $('taskStatus').value,
            dueDate: $('taskDueDate').value || null
        };

        const saveBtn = $('saveTaskBtn');
        saveBtn.disabled = true;

        try {
            await request(id ? `${API}/${id}` : API, {
                method: id ? 'PUT' : 'POST',
                body: JSON.stringify(payload)
            });
            taskModal.hide();
            showToast(id ? 'Task updated' : 'Task created');
            await refresh();
        } catch (err) {
            if (err.fieldErrors) {
                Object.entries(err.fieldErrors).forEach(([field, message]) => {
                    const inputId = field === 'title' ? 'taskTitle'
                        : field === 'description' ? 'taskDescription' : null;
                    if (inputId) {
                        $(inputId).classList.add('is-invalid');
                        $(`${inputId}Error`).textContent = message;
                    }
                });
            } else {
                $('formError').textContent = err.message;
                $('formError').classList.remove('d-none');
            }
        } finally {
            saveBtn.disabled = false;
        }
    }

    // ---------- events ----------

    $('addTaskBtn').addEventListener('click', () => openForm(null));
    $('taskForm').addEventListener('submit', saveTask);

    $('searchInput').addEventListener('input', () => {
        clearTimeout(searchTimer);
        searchTimer = setTimeout(refresh, 300);
    });
    $('filterStatus').addEventListener('change', refresh);
    $('filterPriority').addEventListener('change', refresh);
    $('clearFiltersBtn').addEventListener('click', () => {
        $('searchInput').value = '';
        $('filterStatus').value = '';
        $('filterPriority').value = '';
        refresh();
    });

    $('taskTableBody').addEventListener('click', (event) => {
        const btn = event.target.closest('button[data-action]');
        if (!btn) return;
        const task = tasks.find((t) => String(t.id) === btn.dataset.id);
        if (!task) return;

        if (btn.dataset.action === 'edit') {
            openForm(task);
        } else if (btn.dataset.action === 'delete') {
            deleteId = task.id;
            $('deleteTaskName').textContent = task.title;
            deleteModal.show();
        }
    });

    $('taskTableBody').addEventListener('change', async (event) => {
        const select = event.target.closest('select.status-select');
        if (!select) return;
        try {
            await request(`${API}/${select.dataset.id}/status`, {
                method: 'PATCH',
                body: JSON.stringify({ status: select.value })
            });
            showToast('Status updated');
            await refresh();
        } catch (err) {
            showToast(err.message, 'danger');
            await refresh();
        }
    });

    $('confirmDeleteBtn').addEventListener('click', async () => {
        if (deleteId === null) return;
        try {
            await request(`${API}/${deleteId}`, { method: 'DELETE' });
            deleteModal.hide();
            showToast('Task deleted');
            deleteId = null;
            await refresh();
        } catch (err) {
            deleteModal.hide();
            showToast(err.message, 'danger');
            await refresh();
        }
    });

    // ---------- start ----------
    refresh();
})();