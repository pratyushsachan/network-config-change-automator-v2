import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { AlertCircle, CheckCircle, Clock, Zap, List, Settings, LogOut } from 'lucide-react';
import DeviceTab from './pages/DeviceTab';
import ChangeTab from './pages/ChangeTab';
import DryRunTab from './pages/DryRunTab';
import AuditTab from './pages/AuditTab';

function App() {
  const [activeTab, setActiveTab] = useState('changes');
  const [user] = useState('alice');

  const tabs = [
    { id: 'changes', label: 'Changes', icon: Zap },
    { id: 'devices', label: 'Devices', icon: Settings },
    { id: 'dryrun', label: 'Dry Run', icon: AlertCircle },
    { id: 'audit', label: 'Audit', icon: List },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900">
      {/* Header */}
      <motion.header
        initial={{ y: -100, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.5 }}
        className="bg-white/10 backdrop-blur-md border-b border-white/10"
      >
        <div className="max-w-7xl mx-auto px-6 py-4 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <motion.div
              animate={{ rotate: 360 }}
              transition={{ duration: 3, repeat: Infinity, ease: 'linear' }}
              className="text-white"
            >
              <Zap size={28} />
            </motion.div>
            <div>
              <h1 className="text-2xl font-bold text-white">NetShift</h1>
              <p className="text-xs text-purple-200">Network Config Change Automator</p>
            </div>
          </div>
          <div className="flex items-center gap-4">
            <span className="text-sm text-purple-200">User: <span className="font-semibold">{user}</span></span>
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              className="p-2 rounded-lg bg-red-500/20 hover:bg-red-500/30 text-red-200"
            >
              <LogOut size={20} />
            </motion.button>
          </div>
        </div>
      </motion.header>

      {/* Tabs */}
      <motion.nav
        initial={{ y: -50, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.5, delay: 0.1 }}
        className="bg-white/5 backdrop-blur-sm border-b border-white/10 sticky top-0 z-40"
      >
        <div className="max-w-7xl mx-auto px-6">
          <div className="flex gap-1 py-4">
            {tabs.map((tab) => {
              const Icon = tab.icon;
              return (
                <motion.button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  className={`flex items-center gap-2 px-4 py-2 rounded-lg font-medium transition-all ${
                    activeTab === tab.id
                      ? 'bg-purple-500 text-white shadow-lg shadow-purple-500/50'
                      : 'text-purple-200 hover:bg-white/10'
                  }`}
                >
                  <Icon size={18} />
                  {tab.label}
                </motion.button>
              );
            })}
          </div>
        </div>
      </motion.nav>

      {/* Content */}
      <motion.main
        key={activeTab}
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, y: -20 }}
        transition={{ duration: 0.3 }}
        className="max-w-7xl mx-auto px-6 py-8"
      >
        {activeTab === 'devices' && <DeviceTab />}
        {activeTab === 'changes' && <ChangeTab user={user} />}
        {activeTab === 'dryrun' && <DryRunTab />}
        {activeTab === 'audit' && <AuditTab />}
      </motion.main>

      {/* Footer */}
      <motion.footer
        initial={{ y: 50, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.5, delay: 0.2 }}
        className="mt-20 bg-white/5 backdrop-blur-sm border-t border-white/10 py-6"
      >
        <div className="max-w-7xl mx-auto px-6 text-center text-sm text-purple-300">
          <p>NetShift Demo • Staged, auditable network configuration rollout</p>
          <p className="mt-2 text-xs text-purple-400">All data is simulated for demonstration</p>
        </div>
      </motion.footer>
    </div>
  );
}

export default App;
