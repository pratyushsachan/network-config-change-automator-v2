export const mockDevices = [
  { id: 1, name: 'sw-del-01', ip: '10.10.1.11', model: 'Catalyst 9300', os: '17.9.4', site: 'Delhi', group: 'campus-switches' },
  { id: 2, name: 'sw-del-02', ip: '10.10.1.12', model: 'Catalyst 9300', os: '17.9.4', site: 'Delhi', group: 'campus-switches' },
  { id: 3, name: 'sw-noi-01', ip: '10.10.2.11', model: 'Catalyst 9200', os: '17.6.5', site: 'Noida', group: 'campus-switches' },
  { id: 4, name: 'rtr-core-01', ip: '10.10.0.1', model: 'ISR 4451', os: '17.9.4', site: 'Delhi', group: 'core-routers' },
  { id: 5, name: 'rtr-core-02', ip: '10.10.0.2', model: 'ISR 4451', os: '17.9.4', site: 'Noida', group: 'core-routers' },
  { id: 6, name: 'sw-lab-01', ip: '10.20.0.11', model: 'Catalyst 9300', os: '17.9.4', site: 'Lab', group: 'lab-switches' },
];

export const mockChanges = [
  {
    id: 1,
    title: 'Update NTP Server',
    reason: 'Timezone compliance',
    group: 'core-routers',
    status: 'DEPLOYED',
    requestedBy: 'alice',
    approvedBy: 'bob',
    createdAt: '2026-10-24T10:30:00Z',
    executions: [
      { deviceId: 4, deviceName: 'rtr-core-01', stage: 'CANARY', status: 'APPLIED' },
      { deviceId: 5, deviceName: 'rtr-core-02', stage: 'BATCH-1', status: 'APPLIED' },
    ]
  },
];

export const mockAudit = [
  { id: 1, changeId: 1, actor: 'alice', action: 'CREATED', detail: 'Change created', timestamp: '2026-10-24T10:30:00Z' },
  { id: 2, changeId: 1, actor: 'bob', action: 'APPROVED', detail: 'Change approved', timestamp: '2026-10-24T10:35:00Z' },
  { id: 3, changeId: 1, actor: 'alice', action: 'DEPLOYED', detail: 'Deployment started', timestamp: '2026-10-24T10:45:00Z' },
];
