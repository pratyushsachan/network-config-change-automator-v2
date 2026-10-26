import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { Plus, CheckCircle, Clock, AlertCircle } from 'lucide-react';
import { mockChanges } from '../mockData';

export default function ChangeTab({ user }) {
  const [changes, setChanges] = useState(mockChanges);
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({
    title: '',
    reason: '',
    group: 'core-routers',
    configLines: '',
  });

  const handleCreateChange = () => {
    if (formData.title && formData.configLines) {
      const newChange = {
        id: changes.length + 1,
        ...formData,
        status: 'PENDING_APPROVAL',
        requestedBy: user,
        approvedBy: null,
        createdAt: new Date().toISOString(),
        executions: [],
      };
      setChanges([newChange, ...changes]);
      setFormData({ title: '', reason: '', group: 'core-routers', configLines: '' });
      setShowForm(false);
    }
  };

  const handleApprove = (id) => {
    setChanges(changes.map(c =>
      c.id === id ? { ...c, status: 'APPROVED', approvedBy: 'bob' } : c
    ));
  };

  const handleDeploy = (id) => {
    setChanges(changes.map(c =>
      c.id === id ? { ...c, status: 'DEPLOYED', executions: [
        { deviceId: 1, deviceName: 'device-1', stage: 'CANARY', status: 'APPLIED' },
        { deviceId: 2, deviceName: 'device-2', stage: 'BATCH-1', status: 'APPLIED' },
      ]} : c
    ));
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING_APPROVAL':
        return 'from-yellow-500/20 to-orange-500/20 border-yellow-400/30';
      case 'APPROVED':
        return 'from-blue-500/20 to-cyan-500/20 border-blue-400/30';
      case 'DEPLOYED':
        return 'from-green-500/20 to-emerald-500/20 border-green-400/30';
      default:
        return 'from-gray-500/20 to-slate-500/20 border-gray-400/30';
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'PENDING_APPROVAL':
        return <Clock size={20} className="text-yellow-400" />;
      case 'APPROVED':
        return <AlertCircle size={20} className="text-blue-400" />;
      case 'DEPLOYED':
        return <CheckCircle size={20} className="text-green-400" />;
      default:
        return null;
    }
  };

  return (
    <div className="space-y-6">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="flex justify-between items-center"
      >
        <div>
          <h2 className="text-2xl font-bold text-white">Configuration Changes</h2>
          <p className="text-purple-200">Request, approve, and deploy network changes</p>
        </div>
        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          onClick={() => setShowForm(!showForm)}
          className="flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-purple-500 to-pink-500 text-white rounded-lg font-semibold hover:shadow-lg hover:shadow-purple-500/50 transition-all"
        >
          <Plus size={20} />
          New Change
        </motion.button>
      </motion.div>

      {showForm && (
        <motion.form
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="bg-white/10 backdrop-blur-md rounded-2xl border border-white/20 p-6 space-y-4"
        >
          <input
            type="text"
            placeholder="Change title"
            value={formData.title}
            onChange={(e) => setFormData({ ...formData, title: e.target.value })}
            className="w-full px-4 py-2 rounded-lg bg-white/10 border border-white/20 text-white placeholder-purple-300 focus:outline-none focus:border-purple-400"
          />
          <textarea
            placeholder="Reason for change"
            value={formData.reason}
            onChange={(e) => setFormData({ ...formData, reason: e.target.value })}
            className="w-full px-4 py-2 rounded-lg bg-white/10 border border-white/20 text-white placeholder-purple-300 focus:outline-none focus:border-purple-400 h-20"
          />
          <select
            value={formData.group}
            onChange={(e) => setFormData({ ...formData, group: e.target.value })}
            className="w-full px-4 py-2 rounded-lg bg-white/10 border border-white/20 text-white focus:outline-none focus:border-purple-400"
          >
            <option value="core-routers">Core Routers</option>
            <option value="campus-switches">Campus Switches</option>
            <option value="lab-switches">Lab Switches</option>
          </select>
          <textarea
            placeholder="Config lines (one per line)"
            value={formData.configLines}
            onChange={(e) => setFormData({ ...formData, configLines: e.target.value })}
            className="w-full px-4 py-2 rounded-lg bg-white/10 border border-white/20 text-white placeholder-purple-300 focus:outline-none focus:border-purple-400 h-24 font-mono text-sm"
          />
          <div className="flex gap-4">
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              type="button"
              onClick={handleCreateChange}
              className="px-6 py-2 bg-green-500 text-white rounded-lg font-semibold hover:bg-green-600"
            >
              Create
            </motion.button>
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              type="button"
              onClick={() => setShowForm(false)}
              className="px-6 py-2 bg-gray-500 text-white rounded-lg font-semibold hover:bg-gray-600"
            >
              Cancel
            </motion.button>
          </div>
        </motion.form>
      )}

      <div className="space-y-4">
        {changes.map((change, idx) => (
          <motion.div
            key={change.id}
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.1 + idx * 0.1 }}
            whileHover={{ scale: 1.01 }}
            className={`bg-gradient-to-r ${getStatusColor(change.status)} backdrop-blur-md rounded-xl border p-6 hover:border-white/40 transition-all`}
          >
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <div className="flex items-center gap-3 mb-2">
                  {getStatusIcon(change.status)}
                  <h3 className="text-xl font-bold text-white">{change.title}</h3>
                  <span className="px-3 py-1 rounded-full bg-white/20 text-white text-xs font-semibold">
                    {change.status}
                  </span>
                </div>
                <p className="text-purple-200 mb-3">{change.reason}</p>
                <div className="flex flex-wrap gap-4 text-sm text-purple-300">
                  <span><strong>Group:</strong> {change.group}</span>
                  <span><strong>Requested:</strong> {change.requestedBy}</span>
                  {change.approvedBy && <span><strong>Approved:</strong> {change.approvedBy}</span>}
                </div>
              </div>
              <div className="flex gap-2 ml-4">
                {change.status === 'PENDING_APPROVAL' && (
                  <motion.button
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.95 }}
                    onClick={() => handleApprove(change.id)}
                    className="px-4 py-2 bg-blue-500 text-white rounded-lg font-semibold hover:bg-blue-600"
                  >
                    Approve
                  </motion.button>
                )}
                {change.status === 'APPROVED' && (
                  <motion.button
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.95 }}
                    onClick={() => handleDeploy(change.id)}
                    className="px-4 py-2 bg-green-500 text-white rounded-lg font-semibold hover:bg-green-600"
                  >
                    Deploy
                  </motion.button>
                )}
              </div>
            </div>
          </motion.div>
        ))}
      </div>
    </div>
  );
}
