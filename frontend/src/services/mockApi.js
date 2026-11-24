// ============================================================================
//  MOCK DATA SERVICE — PREVIEW ONLY
// ============================================================================
//  IMPORTANT: This module contains 100% fictional, in-memory data. The current
//  backend exposes NO REST API (no @RestController exists in the Java code),
//  so NOTHING on this site talks to a server or database. Every action
//  (add, edit, book, issue...) happens only inside this module's in-memory
//  arrays and is lost on refresh.
//
//  The data shapes mirror the real Java backend models (Player, Team,
//  Tournament, FacilityBooking, Equipment, Match, Coach, Registration,
//  Payment) and enums from com.thapar.sportssphere.model.enums so that a
//  future REST layer could replace this module with a thin fetch adapter.
//
//  If controllers are added to the backend later, implement real fetch calls
//  here (keeping the same function signatures) and this becomes a live API.
// ============================================================================

export const SPORTS = ['Football', 'Basketball', 'Badminton', 'Cricket', 'Volleyball', 'Table Tennis']
export const TOURNAMENT_STATUSES = ['DRAFT', 'REGISTRATION_OPEN', 'REGISTRATION_CLOSED', 'IN_PROGRESS', 'COMPLETED']
export const MATCH_STATUSES = ['SCHEDULED', 'COMPLETED', 'CANCELLED']
export const REGISTRATION_STATUSES = ['PENDING', 'CONFIRMED', 'CANCELLED']
export const BOOKING_STATUSES = ['CONFIRMED', 'CANCELLED']
export const CONDITIONS = ['EXCELLENT', 'GOOD', 'WORN', 'NEEDS_REPAIR', 'RETIRED']

const delay = (ms = 250) => new Promise(r => setTimeout(r, ms))
const uid = (p) => `${p}-${Math.random().toString(36).slice(2, 7).toUpperCase()}`

let players = [
  { id: 'PL-1001', name: 'Arjun Malhotra', age: 20, contact: 'arjun.m@thapar.edu', rollNo: '102103045', preferences: ['Cricket', 'Football'], registered: true, matchesPlayed: 12, matchesWon: 8, matchesLost: 3, matchesDrawn: 1, pointsScored: 214 },
  { id: 'PL-1002', name: 'Sanya Kapoor', age: 19, contact: 'sanya.k@thapar.edu', rollNo: '102103088', preferences: ['Badminton'], registered: true, matchesPlayed: 9, matchesWon: 7, matchesLost: 2, matchesDrawn: 0, pointsScored: 156 },
  { id: 'PL-1003', name: 'Rohan Verma', age: 21, contact: 'rohan.v@thapar.edu', rollNo: '102103112', preferences: ['Basketball', 'Volleyball'], registered: false, matchesPlayed: 6, matchesWon: 2, matchesLost: 4, matchesDrawn: 0, pointsScored: 88 },
  { id: 'PL-1004', name: 'Ishita Sharma', age: 20, contact: 'ishita.s@thapar.edu', rollNo: '102103201', preferences: ['Table Tennis'], registered: true, matchesPlayed: 11, matchesWon: 9, matchesLost: 1, matchesDrawn: 1, pointsScored: 178 },
  { id: 'PL-1005', name: 'Kabir Singh', age: 22, contact: 'kabir.s@thapar.edu', rollNo: '102103277', preferences: ['Football'], registered: true, matchesPlayed: 14, matchesWon: 10, matchesLost: 2, matchesDrawn: 2, pointsScored: 205 },
  { id: 'PL-1006', name: 'Meera Nair', age: 19, contact: 'meera.n@thapar.edu', rollNo: '102103310', preferences: ['Badminton', 'Table Tennis'], registered: true, matchesPlayed: 8, matchesWon: 5, matchesLost: 3, matchesDrawn: 0, pointsScored: 121 },
  { id: 'PL-1007', name: 'Dev Patel', age: 20, contact: 'dev.p@thapar.edu', rollNo: '102103355', preferences: ['Cricket', 'Volleyball'], registered: false, matchesPlayed: 3, matchesWon: 1, matchesLost: 2, matchesDrawn: 0, pointsScored: 44 },
  { id: 'PL-1008', name: 'Ananya Gupta', age: 21, contact: 'ananya.g@thapar.edu', rollNo: '102103402', preferences: ['Basketball'], registered: true, matchesPlayed: 10, matchesWon: 6, matchesLost: 4, matchesDrawn: 0, pointsScored: 143 },
]

let coaches = [
  { id: 'CO-2001', name: 'Vikram Rathore', age: 47, contact: 'vikram.rathore@thapar.edu', specialty: 'Cricket', yearsExperience: 18 },
  { id: 'CO-2002', name: 'Pooja Bedi', age: 39, contact: 'pooja.bedi@thapar.edu', specialty: 'Badminton', yearsExperience: 11 },
  { id: 'CO-2003', name: 'Harpreet Gill', age: 52, contact: 'harpreet.gill@thapar.edu', specialty: 'Football', yearsExperience: 24 },
  { id: 'CO-2004', name: 'Nisha Menon', age: 44, contact: 'nisha.menon@thapar.edu', specialty: 'Basketball', yearsExperience: 15 },
]

let teams = [
  { id: 'TM-3001', name: 'Thunder Strikers', sport: 'Cricket', captain: 'PL-1001', roster: ['PL-1001', 'PL-1007'], maxTeamSize: 15 },
  { id: 'TM-3002', name: 'Court Kings', sport: 'Basketball', captain: 'PL-1008', roster: ['PL-1008', 'PL-1003'], maxTeamSize: 12 },
  { id: 'TM-3003', name: 'Smash Squad', sport: 'Badminton', captain: 'PL-1002', roster: ['PL-1002', 'PL-1006'], maxTeamSize: 4 },
  { id: 'TM-3004', name: 'Goal Guardians', sport: 'Football', captain: 'PL-1005', roster: ['PL-1005'], maxTeamSize: 18 },
]

let tournaments = [
  { id: 'TR-4001', name: 'Inter-Hostel Cricket Cup', sport: 'Cricket', registrationDeadline: '2026-11-05', maxEntries: 16, feeMultiplier: 1.2, status: 'REGISTRATION_OPEN', registrations: [{ id: 'RG-9001', entrant: 'Thunder Strikers', type: 'TEAM', status: 'CONFIRMED' }, { id: 'RG-9002', entrant: 'Dev Patel', type: 'PLAYER', status: 'PENDING' }] },
  { id: 'TR-4002', name: 'Championship Badminton Open', sport: 'Badminton', registrationDeadline: '2026-10-28', maxEntries: 32, feeMultiplier: 1.0, status: 'REGISTRATION_OPEN', registrations: [{ id: 'RG-9003', entrant: 'Sanya Kapoor', type: 'PLAYER', status: 'CONFIRMED' }, { id: 'RG-9004', entrant: 'Meera Nair', type: 'PLAYER', status: 'CONFIRMED' }] },
  { id: 'TR-4003', name: 'Winter Basketball League', sport: 'Basketball', registrationDeadline: '2026-11-15', maxEntries: 8, feeMultiplier: 1.5, status: 'DRAFT', registrations: [] },
  { id: 'TR-4004', name: 'Monsoon Football Fiesta', sport: 'Football', registrationDeadline: '2026-09-30', maxEntries: 12, feeMultiplier: 1.0, status: 'IN_PROGRESS', registrations: [{ id: 'RG-9005', entrant: 'Goal Guardians', type: 'TEAM', status: 'CONFIRMED' }] },
]

const TODAY = new Date().toISOString().slice(0, 10)
const day = (offset) => {
  const d = new Date(); d.setDate(d.getDate() + offset); return d.toISOString().slice(0, 10)
}

let bookings = [
  { id: 'BK-5001', facility: 'Main Football Ground', sport: 'Football', date: day(1), slot: '16:00-17:00', bookedBy: 'Goal Guardians', status: 'CONFIRMED' },
  { id: 'BK-5002', facility: 'Indoor Badminton Courts', sport: 'Badminton', date: TODAY, slot: '18:00-19:00', bookedBy: 'Smash Squad', status: 'CONFIRMED' },
  { id: 'BK-5003', facility: 'Basketball Arena', sport: 'Basketball', date: day(2), slot: '17:00-18:00', bookedBy: 'Court Kings', status: 'CONFIRMED' },
  { id: 'BK-5004', facility: 'Cricket Practice Nets', sport: 'Cricket', date: day(3), slot: '06:00-07:00', bookedBy: 'Thunder Strikers', status: 'CANCELLED' },
]

export const FACILITIES = [
  { name: 'Main Football Ground', sport: 'Football', capacity: 30 },
  { name: 'Indoor Badminton Courts', sport: 'Badminton', capacity: 8 },
  { name: 'Basketball Arena', sport: 'Basketball', capacity: 20 },
  { name: 'Cricket Practice Nets', sport: 'Cricket', capacity: 12 },
  { name: 'Volleyball Court', sport: 'Volleyball', capacity: 14 },
  { name: 'Table Tennis Hall', sport: 'Table Tennis', capacity: 10 },
]
export const SLOTS = ['06:00-07:00', '07:00-08:00', '16:00-17:00', '17:00-18:00', '18:00-19:00', '19:00-20:00']

let equipment = [
  { id: 'EQ-6001', name: 'Cricket Bat (Pro)', sport: 'Cricket', condition: 'GOOD', totalUnits: 15, availableUnits: 11 },
  { id: 'EQ-6002', name: 'Football — Size 5', sport: 'Football', condition: 'EXCELLENT', totalUnits: 24, availableUnits: 20 },
  { id: 'EQ-6003', name: 'Badminton Racket', sport: 'Badminton', condition: 'GOOD', totalUnits: 30, availableUnits: 22 },
  { id: 'EQ-6004', name: 'Basketball — Indoor', sport: 'Basketball', condition: 'WORN', totalUnits: 10, availableUnits: 4 },
  { id: 'EQ-6005', name: 'Volleyball Net Set', sport: 'Volleyball', condition: 'NEEDS_REPAIR', totalUnits: 3, availableUnits: 1 },
  { id: 'EQ-6006', name: 'TT Paddle', sport: 'Table Tennis', condition: 'EXCELLENT', totalUnits: 20, availableUnits: 16 },
]

let issued = [
  { id: 'IS-7001', equipmentId: 'EQ-6002', item: 'Football — Size 5', toPlayer: 'PL-1001', playerName: 'Arjun Malhotra', issuedOn: TODAY },
  { id: 'IS-7002', equipmentId: 'EQ-6003', item: 'Badminton Racket', toPlayer: 'PL-1002', playerName: 'Sanya Kapoor', issuedOn: TODAY },
]

let matches = [
  { id: 'MT-8001', tournament: 'Monsoon Football Fiesta', sport: 'Football', home: 'Goal Guardians', away: 'Thunder Strikers', date: day(4), slot: '16:00-17:00', status: 'SCHEDULED', homeScore: null, awayScore: null },
  { id: 'MT-8002', tournament: 'Championship Badminton Open', sport: 'Badminton', home: 'Sanya Kapoor', away: 'Meera Nair', date: TODAY, slot: '18:00-19:00', status: 'COMPLETED', homeScore: 21, awayScore: 15 },
  { id: 'MT-8003', tournament: 'Monsoon Football Fiesta', sport: 'Football', home: 'Goal Guardians', away: 'Court Kings', date: day(5), slot: '17:00-18:00', status: 'SCHEDULED', homeScore: null, awayScore: null },
  { id: 'MT-8004', tournament: 'Championship Badminton Open', sport: 'Badminton', home: 'Arjun Malhotra', away: 'Ananya Gupta', date: day(-1), slot: '19:00-20:00', status: 'COMPLETED', homeScore: 18, awayScore: 21 },
]

let payments = [
  { id: 'PAY-9501', player: 'Arjun Malhotra', description: 'Inter-Hostel Cricket Cup entry', amount: 240, status: 'PAID', date: day(-3) },
  { id: 'PAY-9502', player: 'Sanya Kapoor', description: 'Badminton Open entry', amount: 200, status: 'PAID', date: day(-2) },
  { id: 'PAY-9503', player: 'Rohan Verma', description: 'Basketball League entry', amount: 300, status: 'PENDING', date: TODAY },
]

// -------- activity feed --------
let activity = [
  { id: 1, icon: '🏸', text: 'Sanya Kapoor booked Indoor Badminton Courts (18:00-19:00)', time: '2h ago' },
  { id: 2, icon: '🏆', text: 'Inter-Hostel Cricket Cup opened registration', time: '5h ago' },
  { id: 3, icon: '🏸', text: 'Match completed: Sanya Kapoor 21 – 15 Meera Nair', time: '1d ago' },
  { id: 4, icon: '⚽', text: 'Equipment issued: Football — Size 5 → Arjun Malhotra', time: '1d ago' },
  { id: 5, icon: '🥇', text: 'Ishita Sharma won Table Tennis ladder match', time: '2d ago' },
]

const logActivity = (icon, text) => {
  activity = [{ id: Date.now(), icon, text, time: 'just now' }, ...activity].slice(0, 12)
}

// ============================ API (mock) ====================================

const find = (arr, id) => arr.find(x => x.id === id)

export const api = {
  // -- dashboard
  async getDashboard() {
    await delay()
    const registered = players.filter(p => p.registered).length
    const openT = tournaments.filter(t => t.status === 'REGISTRATION_OPEN').length
    return {
      stats: [
        { label: 'Players', value: players.length, sub: `${registered} registered`, icon: '🏃', tone: 'blue' },
        { label: 'Teams', value: teams.length, sub: `${teams.reduce((s, t) => s + t.roster.length, 0)} roster spots filled`, icon: '👥', tone: 'violet' },
        { label: 'Tournaments', value: tournaments.length, sub: `${openT} open for registration`, icon: '🏆', tone: 'amber' },
        { label: 'Active Bookings', value: bookings.filter(b => b.status === 'CONFIRMED').length, sub: 'confirmed slots', icon: '📅', tone: 'green' },
      ],
      activity,
    }
  },

  // -- players
  async getPlayers() { await delay(); return [...players] },
  async addPlayer(data) {
    await delay()
    if (!data.name?.trim()) throw new Error('Name is required')
    if (!data.rollNo?.trim()) throw new Error('Roll number is required')
    if (players.some(p => p.rollNo === data.rollNo.trim())) throw new Error(`A player with roll number ${data.rollNo} already exists`)
    if (!(data.age >= 16 && data.age <= 40)) throw new Error('Age must be between 16 and 40')
    const p = { id: uid('PL'), matchesPlayed: 0, matchesWon: 0, matchesLost: 0, matchesDrawn: 0, pointsScored: 0, contact: 'N/A', ...data, preferences: data.preferences || [] }
    players = [p, ...players]; logActivity('🏃', `${p.name} joined as a player`); return p
  },
  async updatePlayer(id, data) {
    await delay()
    const p = find(players, id); if (!p) throw new Error('Player not found')
    if (!data.name?.trim()) throw new Error('Name is required')
    Object.assign(p, data); logActivity('✏️', `${p.name}'s profile was updated`); return p
  },
  async toggleRegistration(id) {
    await delay()
    const p = find(players, id); if (!p) throw new Error('Player not found')
    p.registered = !p.registered
    logActivity(p.registered ? '✅' : '🚫', `${p.name} ${p.registered ? 'registered with' : 'deregistered from'} the sports office`)
    return p
  },

  // -- coaches
  async getCoaches() { await delay(); return [...coaches] },
  async addCoach(data) {
    await delay()
    if (!data.name?.trim()) throw new Error('Name is required')
    if (!(data.yearsExperience >= 0)) throw new Error('Experience cannot be negative')
    const c = { id: uid('CO'), ...data }; coaches = [c, ...coaches]; logActivity('🎓', `${c.name} joined as a ${c.specialty} coach`); return c
  },

  // -- teams
  async getTeams() {
    await delay()
    return teams.map(t => ({
      ...t,
      captainName: find(players, t.captain)?.name || '—',
      rosterDetails: t.roster.map(id => ({ id, name: find(players, id)?.name || id })),
    }))
  },
  async addTeam(data) {
    await delay()
    if (!data.name?.trim()) throw new Error('Team name is required')
    const t = { id: uid('TM'), roster: [], captain: null, ...data, maxTeamSize: 15 }
    teams = [t, ...teams]; logActivity('👥', `New team "${t.name}" created`); return t
  },
  async addRosterPlayer(teamId, playerId) {
    await delay()
    const t = find(teams, teamId); if (!t) throw new Error('Team not found')
    const p = find(players, playerId); if (!p) throw new Error('Player not found')
    if (t.roster.includes(playerId)) throw new Error(`${p.name} is already in ${t.name}`)
    if (t.roster.length >= t.maxTeamSize) throw new Error(`${t.name} is full (max ${t.maxTeamSize} players)`)
    t.roster.push(playerId); logActivity('➕', `${p.name} added to ${t.name}`)
  },
  async removeRosterPlayer(teamId, playerId) {
    await delay()
    const t = find(teams, teamId); if (!t) throw new Error('Team not found')
    t.roster = t.roster.filter(id => id !== playerId)
    if (t.captain === playerId) t.captain = null
    logActivity('➖', `${find(players, playerId)?.name || 'Player'} removed from ${t.name}`)
  },
  async setCaptain(teamId, playerId) {
    await delay()
    const t = find(teams, teamId); if (!t) throw new Error('Team not found')
    if (!t.roster.includes(playerId)) throw new Error('Captain must be on the roster')
    t.captain = playerId; logActivity('🧑‍✈️', `${find(players, playerId)?.name} named captain of ${t.name}`)
  },

  // -- tournaments
  async getTournaments() { await delay(); return tournaments.map(t => ({ ...t, entries: t.registrations.filter(r => r.status !== 'CANCELLED').length })) },
  async addTournament(data) {
    await delay()
    if (!data.name?.trim()) throw new Error('Tournament name is required')
    if (!(data.maxEntries >= 2 && data.maxEntries <= 64)) throw new Error('Maximum entries must be between 2 and 64')
    const t = { id: uid('TR'), status: 'DRAFT', registrations: [], feeMultiplier: 1.0, ...data }
    tournaments = [t, ...tournaments]; logActivity('🏆', `Tournament "${t.name}" created as draft`)
  },
  async advanceTournament(id) {
    await delay()
    const t = find(tournaments, id); if (!t) throw new Error('Tournament not found')
    const order = ['DRAFT', 'REGISTRATION_OPEN', 'REGISTRATION_CLOSED', 'IN_PROGRESS', 'COMPLETED']
    const i = order.indexOf(t.status)
    if (i === order.length - 1) throw new Error('Tournament is already completed')
    t.status = order[i + 1]; logActivity('🏆', `${t.name} → ${t.status.replaceAll('_', ' ').toLowerCase()}`)
  },
  async registerForTournament(tournamentId, entrantName, type) {
    await delay()
    const t = find(tournaments, tournamentId); if (!t) throw new Error('Tournament not found')
    if (t.status !== 'REGISTRATION_OPEN') throw new Error('Registration is not open for this tournament')
    const active = t.registrations.filter(r => r.status !== 'CANCELLED')
    if (active.length >= t.maxEntries) throw new Error('Tournament is full')
    if (active.some(r => r.entrant === entrantName)) throw new Error(`${entrantName} is already registered`)
    t.registrations.push({ id: uid('RG'), entrant: entrantName, type, status: 'PENDING' })
    logActivity('📝', `${entrantName} registered for ${t.name}`)
  },

  // -- facilities & bookings
  async getSchedule(date) {
    await delay(350)
    const rows = FACILITIES.map(f => ({
      ...f,
      slots: SLOTS.map(slot => {
        const booking = bookings.find(b => b.facility === f.name && b.date === date && b.slot === slot && b.status === 'CONFIRMED')
        return { slot, booking: booking || null }
      }),
    }))
    return { date, rows }
  },
  async bookSlot({ facility, date, slot, bookedBy }) {
    await delay()
    if (!bookedBy?.trim()) throw new Error('Team / player name is required')
    if (date < TODAY) throw new Error('Cannot book a slot in the past')
    const clash = bookings.find(b => b.facility === facility && b.date === date && b.slot === slot && b.status === 'CONFIRMED')
    if (clash) throw new Error(`${facility} is already booked for ${slot} on ${date} (by ${clash.bookedBy})`)
    const b = { id: uid('BK'), facility, date, slot, bookedBy: bookedBy.trim(), status: 'CONFIRMED', sport: FACILITIES.find(f => f.name === facility)?.sport || '—' }
    bookings = [b, ...bookings]; logActivity('📅', `${bookedBy} booked ${facility} (${slot})`)
  },
  async cancelBooking(id) {
    await delay()
    const b = find(bookings, id); if (!b) throw new Error('Booking not found')
    b.status = 'CANCELLED'; logActivity('❌', `${b.facility} booking on ${b.date} cancelled`)
  },

  // -- equipment
  async getEquipment() { await delay(); return equipment.map(e => ({ ...e, issuedUnits: e.totalUnits - e.availableUnits })) },
  async addEquipment(data) {
    await delay()
    if (!data.name?.trim()) throw new Error('Item name is required')
    if (!(data.totalUnits > 0)) throw new Error('Total units must be positive')
    const e = { id: uid('EQ'), availableUnits: data.totalUnits, condition: 'GOOD', ...data }
    equipment = [e, ...equipment]; logActivity('🎽', `${e.totalUnits} × ${e.name} added to inventory`)
  },
  async issueEquipment(equipmentId, toPlayerId) {
    await delay()
    const e = find(equipment, equipmentId); if (!e) throw new Error('Equipment not found')
    const p = find(players, toPlayerId); if (!p) throw new Error('Player not found')
    if (e.availableUnits <= 0) throw new Error(`No ${e.name} units are currently available`)
    e.availableUnits -= 1
    issued = [{ id: uid('IS'), equipmentId, item: e.name, toPlayer: p.id, playerName: p.name, issuedOn: TODAY }, ...issued]
    logActivity('🎽', `${e.name} issued to ${p.name}`)
  },
  async returnEquipment(issueId) {
    await delay()
    const i = find(issued, issueId); if (!i) throw new Error('This item is not on issue')
    const e = find(equipment, i.equipmentId)
    if (e) e.availableUnits += 1
    issued = issued.filter(x => x.id !== issueId)
    logActivity('↩️', `${i.item} returned by ${i.playerName}`)
  },
  async getIssued() { await delay(); return [...issued] },

  // -- matches
  async getMatches() { await delay(); return [...matches] },
  async scheduleMatch(data) {
    await delay()
    if (!data.home?.trim() || !data.away?.trim()) throw new Error('Both participants are required')
    if (data.home.trim() === data.away.trim()) throw new Error('A team cannot play itself')
    const m = { id: uid('MT'), status: 'SCHEDULED', homeScore: null, awayScore: null, ...data }
    matches = [m, ...matches]; logActivity('🗓️', `Match scheduled: ${m.home} vs ${m.away}`)
  },
  async recordScore(id, homeScore, awayScore) {
    await delay()
    const m = find(matches, id); if (!m) throw new Error('Match not found')
    if (m.status === 'CANCELLED') throw new Error('Cannot record a score for a cancelled match')
    if (!(homeScore >= 0 && awayScore >= 0)) throw new Error('Scores cannot be negative')
    m.homeScore = homeScore; m.awayScore = awayScore; m.status = 'COMPLETED'
    logActivity('🏅', `Result: ${m.home} ${homeScore} – ${awayScore} ${m.away}`)
  },
  async cancelMatch(id) {
    await delay()
    const m = find(matches, id); if (!m) throw new Error('Match not found')
    if (m.status === 'COMPLETED') throw new Error('Cannot cancel a completed match')
    m.status = 'CANCELLED'; logActivity('❌', `${m.home} vs ${m.away} cancelled`)
  },

  // -- analytics & reports
  async getAnalytics() {
    await delay(400)
    const bySport = SPORTS.map(s => ({ sport: s, players: players.filter(p => p.preferences.includes(s)).length }))
    const leaderboard = [...players].sort((a, b) => (b.matchesWon - b.matchesLost) - (a.matchesWon - a.matchesLost) || b.pointsScored - a.pointsScored).slice(0, 5)
    const registeredRate = players.length ? Math.round((players.filter(p => p.registered).length / players.length) * 100) : 0
    return {
      kpis: [
        { label: 'Registration Rate', value: `${registeredRate}%`, icon: '✅' },
        { label: 'Avg Matches / Player', value: (players.reduce((s, p) => s + p.matchesPlayed, 0) / (players.length || 1)).toFixed(1), icon: '📊' },
        { label: 'Equipment Utilisation', value: `${Math.round((issued.length / (equipment.reduce((s, e) => s + e.totalUnits, 0) || 1)) * 100)}%`, icon: '🎽' },
        { label: 'Upcoming Matches', value: matches.filter(m => m.status === 'SCHEDULED').length, icon: '🗓️' },
      ],
      bySport,
      leaderboard,
    }
  },
  async getReports() {
    await delay(400)
    return [
      { title: 'Player Registration Summary', desc: 'Registered vs unregistered players, preferences by sport.', tag: 'Players', icon: '🏃' },
      { title: 'Tournament Status Board', desc: 'Lifecycle stage and entry counts for every tournament.', tag: 'Tournaments', icon: '🏆' },
      { title: 'Facility Utilisation', desc: 'Confirmed bookings per facility and free slots today.', tag: 'Facilities', icon: '📅' },
      { title: 'Equipment Condition Audit', desc: 'Inventory grouped by condition with issue/return activity.', tag: 'Equipment', icon: '🎽' },
      { title: 'Payments Outstanding', desc: 'Pending entry-fee payments with due amounts.', tag: 'Payments', icon: '💰' },
    ]
  },
  async getPayments() { await delay(); return [...payments] },

  // -- persistence controls (preview)
  async saveData() { await delay(600); logActivity('💾', 'Snapshot saved (preview only — nothing was written to disk)') },
  async loadData() { await delay(600); logActivity('📂', 'Snapshot loaded (preview only)') },
}
