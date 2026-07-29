"use client";

import { useState } from 'react';

export default function FlightSearch() {
  const [origin, setOrigin] = useState('');
  const [destination, setDestination] = useState('');
  const [date, setDate] = useState('');
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [results, setResults] = useState<any>(null);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setResults(null);

    try {
      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
      const params = new URLSearchParams({
        origin: origin.toUpperCase(),
        destination: destination.toUpperCase(),
        date
      });
      
      const response = await fetch(`${baseUrl}/api/flights/search?${params.toString()}`);
      
      if (!response.ok) {
        const errData = await response.json().catch(() => null);
        if (errData && errData.message) {
          throw new Error(errData.message);
        } else {
          throw new Error(`Server returned ${response.status}: ${response.statusText}`);
        }
      }

      const data = await response.json();
      setResults(data);
    } catch (err: any) {
      setError(err.message || 'An unexpected error occurred while fetching flights.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen bg-gradient-to-br from-slate-900 to-slate-800 text-white flex flex-col items-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-5xl">
        <div className="text-center mb-12">
          <h1 className="text-5xl font-extrabold tracking-tight mb-4 text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-emerald-400">
            SkyPath
          </h1>
          <p className="text-lg text-slate-300">Find the optimal flight connections across the globe.</p>
        </div>

        <div className="bg-slate-800/50 backdrop-blur-md rounded-2xl shadow-xl border border-slate-700 p-6 md:p-8 mb-8">
          <form onSubmit={handleSearch} className="grid grid-cols-1 md:grid-cols-4 gap-6 items-end">
            <div className="flex flex-col gap-2">
              <label htmlFor="origin" className="text-sm font-medium text-slate-300">Origin (IATA)</label>
              <input
                id="origin"
                type="text"
                maxLength={3}
                required
                placeholder="e.g. JFK"
                className="px-4 py-3 bg-slate-900 border border-slate-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all text-white uppercase placeholder-slate-500"
                value={origin}
                onChange={(e) => setOrigin(e.target.value.toUpperCase())}
              />
            </div>
            
            <div className="flex flex-col gap-2">
              <label htmlFor="destination" className="text-sm font-medium text-slate-300">Destination (IATA)</label>
              <input
                id="destination"
                type="text"
                maxLength={3}
                required
                placeholder="e.g. LHR"
                className="px-4 py-3 bg-slate-900 border border-slate-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all text-white uppercase placeholder-slate-500"
                value={destination}
                onChange={(e) => setDestination(e.target.value.toUpperCase())}
              />
            </div>
            
            <div className="flex flex-col gap-2">
              <label htmlFor="date" className="text-sm font-medium text-slate-300">Date</label>
              <input
                id="date"
                type="date"
                required
                className="px-4 py-3 bg-slate-900 border border-slate-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all text-white color-scheme-dark"
                style={{ colorScheme: 'dark' }}
                value={date}
                onChange={(e) => setDate(e.target.value)}
              />
            </div>
            
            <div className="flex flex-col justify-end h-full">
              <button
                type="submit"
                disabled={loading}
                className="w-full px-6 py-3 bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-500 hover:to-blue-400 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg font-semibold text-white shadow-lg transition-all transform hover:-translate-y-0.5"
              >
                {loading ? (
                  <span className="flex items-center justify-center gap-2">
                    <svg className="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Searching...
                  </span>
                ) : 'Search Flights'}
              </button>
            </div>
          </form>
        </div>

        {error && (
          <div className="mb-8 p-4 bg-red-500/10 border border-red-500/50 rounded-xl">
            <div className="flex items-start gap-3">
              <svg className="w-6 h-6 text-red-400 mt-0.5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
              <div>
                <h3 className="text-red-400 font-semibold mb-1">Search Error</h3>
                <p className="text-red-200/90 text-sm">{error}</p>
              </div>
            </div>
          </div>
        )}

        {results && (
          <div className="bg-slate-800/50 backdrop-blur-md rounded-2xl shadow-xl border border-slate-700 p-6 md:p-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
            <h2 className="text-xl font-bold mb-4 text-white">Search Results</h2>
            {results.length === 0 ? (
              <p className="text-slate-400">No flights found matching your criteria. Try adjusting your dates or airports.</p>
            ) : (
              <div className="overflow-x-auto bg-slate-900 rounded-lg border border-slate-700 p-4">
                <pre className="text-sm text-emerald-400 whitespace-pre-wrap font-mono">
                  {JSON.stringify(results, null, 2)}
                </pre>
              </div>
            )}
          </div>
        )}
      </div>
    </main>
  );
}
